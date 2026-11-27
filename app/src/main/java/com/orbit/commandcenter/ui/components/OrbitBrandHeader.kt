package com.orbit.commandcenter.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbit.commandcenter.R

/**
 * ORBIT wordmark with the mark and the tagline.
 */
@Composable
fun OrbitBrandHeader(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    supportingText: String = stringResource(R.string.app_tagline),
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        OrbitMark(modifier = Modifier.size(46.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
