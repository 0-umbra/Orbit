package com.orbit.commandcenter.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orbit.commandcenter.R
import com.orbit.commandcenter.domain.model.ModuleStatus
import com.orbit.commandcenter.domain.model.OrbitModule
import com.orbit.commandcenter.domain.model.OrbitRoadmap
import com.orbit.commandcenter.ui.components.OrbitBrandHeader
import com.orbit.commandcenter.ui.components.StatusPill
import com.orbit.commandcenter.ui.components.rememberOrbitCardBorder
import com.orbit.commandcenter.ui.theme.OrbitTheme

// Static layout values are hoisted to the file scope: they never change while the app
// runs, so they are allocated once instead of on every composition of the list.
private val DashboardContentPadding =
    PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 36.dp)
private val DashboardItemSpacing: Arrangement.Vertical = Arrangement.spacedBy(14.dp)
private val AboutButtonPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
private val PrivacyNotePadding = Modifier.padding(top = 12.dp)
private val RoadmapHeaderPadding = Modifier.padding(top = 12.dp)
private val WelcomeCardPadding = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)
private val ModuleCardPadding = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)

/** Lets the lazy list reuse slot content for the (uniform) module cards. */
private const val ModuleContentType = "orbit-module"

/**
 * ORBIT launch screen: branding, a short welcome message and the module roadmap that
 * doubles as the dashboard placeholder for Phase 0.
 *
 * The screen is a single [LazyColumn] with stable keys and content types, so scrolling
 * only composes the rows that become visible and never recomposes the ones that stay on
 * screen.
 */
@Composable
fun DashboardScreen(
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        onNavigateToAbout = onNavigateToAbout,
        modifier = modifier,
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = DashboardContentPadding,
            verticalArrangement = DashboardItemSpacing,
        ) {
            item(key = "brand", contentType = "brand") {
                OrbitBrandHeader()
            }
            item(key = "welcome", contentType = "welcome") {
                WelcomeCard()
            }
            item(key = "roadmap-header", contentType = "section-header") {
                RoadmapHeader()
            }
            items(
                items = uiState.modules,
                key = { module -> module.id },
                contentType = { ModuleContentType },
            ) { module ->
                OrbitModuleCard(module = module)
            }
            item(key = "privacy", contentType = "note") {
                Text(
                    text = stringResource(R.string.dashboard_privacy_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = PrivacyNotePadding,
                )
            }
            item(key = "about", contentType = "action") {
                TextButton(
                    onClick = onNavigateToAbout,
                    contentPadding = AboutButtonPadding,
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_about_action),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = rememberOrbitCardBorder(),
    ) {
        Column(modifier = WelcomeCardPadding) {
            StatusPill(text = stringResource(R.string.dashboard_foundation_chip))
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.dashboard_welcome_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.dashboard_welcome_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RoadmapHeader() {
    Column(modifier = RoadmapHeaderPadding) {
        Text(
            text = stringResource(R.string.dashboard_roadmap_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.dashboard_roadmap_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OrbitModuleCard(module: OrbitModule) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = rememberOrbitCardBorder(),
    ) {
        Column(modifier = ModuleCardPadding) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                StatusPill(
                    text = statusLabel(module.status),
                    accent = statusAccent(module.status),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@ReadOnlyComposable
private fun statusLabel(status: ModuleStatus): String = when (status) {
    ModuleStatus.PLANNED -> stringResource(R.string.module_status_planned)
    ModuleStatus.IN_PROGRESS -> stringResource(R.string.module_status_in_progress)
    ModuleStatus.AVAILABLE -> stringResource(R.string.module_status_available)
}

@Composable
@ReadOnlyComposable
private fun statusAccent(status: ModuleStatus): Color = when (status) {
    ModuleStatus.PLANNED -> MaterialTheme.colorScheme.onSurfaceVariant
    ModuleStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
    ModuleStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
}

@Preview(name = "ORBIT dashboard (dark)", showBackground = true)
@Preview(name = "ORBIT dashboard (light)", showBackground = true, uiMode = 0x10)
@Composable
private fun DashboardContentPreview() {
    OrbitTheme {
        DashboardContent(
            uiState = DashboardUiState(modules = OrbitRoadmap.modules, isLoaded = true),
            onNavigateToAbout = {},
        )
    }
}
