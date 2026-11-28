#!/usr/bin/env sh
#
# ORBIT frame-timing measurement over adb — free, reproducible, no Android Studio.
#
# It drives a fixed scroll gesture pattern, then reports the numbers Android's own
# `dumpsys gfxinfo` collects (frames rendered, janky frames, percentile frame times).
#
# Usage:
#   tools/perf/measure-scroll.sh [package] [swipes]
#
# Environment:
#   PACKAGE     default com.orbit.commandcenter
#   SWIPES      default 8        scroll gestures per run
#   COMPILE     default 0        set to 1 to AOT-compile the installed build first
#                                (`cmd package compile -m speed -f`); this shows how much of
#                                the jank is interpreter/JIT cost rather than app code
#   OUT_DIR     default build/perf
#   GFXINFO_FILE  analyse a previously captured `dumpsys gfxinfo` dump instead of
#                                measuring a connected device (parsing only, no adb needed)
#
# Example:
#   ./gradlew installBenchmark
#   tools/perf/measure-scroll.sh            # release-equivalent build
#   ./gradlew installDebug
#   tools/perf/measure-scroll.sh            # same gesture pattern on the debug build
#
set -eu

PACKAGE="${1:-${PACKAGE:-com.orbit.commandcenter}}"
SWIPES="${2:-${SWIPES:-8}}"
COMPILE="${COMPILE:-0}"
OUT_DIR="${OUT_DIR:-build/perf}"

percentile() {
    # $1 = dump file, $2 = label ("50th", "90th", "95th", "99th")
    sed -n "s/.*$2 percentile: \(.*\)/\1/p" "$1" | head -1
}

field() {
    # $1 = dump file, $2 = field name
    sed -n "s/^ *$2: \(.*\)/\1/p" "$1" | head -1
}

summarize() {
    DUMP="$1"
    TOTAL=$(field "$DUMP" "Total frames rendered")
    JANKY=$(field "$DUMP" "Janky frames")
    P50=$(percentile "$DUMP" "50th")
    P90=$(percentile "$DUMP" "90th")
    P95=$(percentile "$DUMP" "95th")
    P99=$(percentile "$DUMP" "99th")
    JANK_PCT=$(echo "$JANKY" | sed -n 's/.*(\([0-9.]*\)%).*/\1/p')

    echo "----------------------------------------------------------------"
    echo "package          : $PACKAGE"
    echo "frames rendered  : ${TOTAL:-unknown}"
    echo "janky frames     : ${JANKY:-unknown}"
    echo "50th percentile  : ${P50:-unknown}"
    echo "90th percentile  : ${P90:-unknown}"
    echo "95th percentile  : ${P95:-unknown}"
    echo "99th percentile  : ${P99:-unknown}"
    echo "raw dump         : $DUMP"
    echo "----------------------------------------------------------------"
    if [ -n "$JANK_PCT" ]; then
        # 5% janky frames is the usual "acceptable" bar for a 60 Hz UI.
        awk -v pct="$JANK_PCT" 'BEGIN {
            if (pct > 20)      verdict = "poor - most frames drop";
            else if (pct > 10) verdict = "significant jank";
            else if (pct > 5)  verdict = "mild jank (above the 5% bar)";
            else               verdict = "smooth (at or below the 5% bar)";
            printf "verdict          : %s (%.2f%% janky)\n", verdict, pct;
        }'
    fi
}

if [ -n "${GFXINFO_FILE:-}" ]; then
    test -f "$GFXINFO_FILE" || { echo "error: GFXINFO_FILE not found: $GFXINFO_FILE" >&2; exit 1; }
    summarize "$GFXINFO_FILE"
    exit 0
fi

command -v adb >/dev/null 2>&1 || { echo "error: adb is not in PATH" >&2; exit 1; }
adb get-state >/dev/null 2>&1 || { echo "error: no device/emulator reachable via adb" >&2; exit 1; }
if ! adb shell pm list packages 2>/dev/null | tr -d '\r' | grep -qx "package:$PACKAGE"; then
    echo "error: $PACKAGE is not installed on the device" >&2
    exit 1
fi

if [ "$COMPILE" = "1" ]; then
    echo "AOT-compiling $PACKAGE (-m speed -f)..."
    adb shell cmd package compile -m speed -f "$PACKAGE" \
        || echo "warning: compile command failed; continuing without it"
fi

SIZE=$(adb shell wm size 2>/dev/null | tr -d '\r' | awk -F': ' '/Physical size/ {print $2; exit}')
[ -n "${SIZE:-}" ] || SIZE="1080x1920"
WIDTH=${SIZE%x*}
HEIGHT=${SIZE#*x}
if [ "$WIDTH" -gt 0 ] 2>/dev/null && [ "$HEIGHT" -gt 0 ] 2>/dev/null; then
    CENTER_X=$((WIDTH / 2))
    START_Y=$((HEIGHT * 3 / 4))
    END_Y=$((HEIGHT / 4))
else
    CENTER_X=540; START_Y=1600; END_Y=500
fi

echo "device screen    : ${WIDTH}x${HEIGHT}"
echo "gestures         : $SWIPES scrolls (${CENTER_X},${START_Y} -> ${CENTER_X},${END_Y})"

mkdir -p "$OUT_DIR"
STAMP=$(date +%Y%m%d-%H%M%S)

# Warm-up: the first scroll of a fresh process compiles code paths and fills caches, so it
# is deliberately excluded from the measured window.
adb shell input swipe "$CENTER_X" "$START_Y" "$CENTER_X" "$END_Y" 220 || true
sleep 1

adb shell dumpsys gfxinfo "$PACKAGE" reset >/dev/null

i=0
while [ "$i" -lt "$SWIPES" ]; do
    adb shell input swipe "$CENTER_X" "$START_Y" "$CENTER_X" "$END_Y" 220 || true
    sleep 0.4
    i=$((i + 1))
done

RAW="$OUT_DIR/gfxinfo-$STAMP.txt"
adb shell dumpsys gfxinfo "$PACKAGE" > "$RAW"
summarize "$RAW"
