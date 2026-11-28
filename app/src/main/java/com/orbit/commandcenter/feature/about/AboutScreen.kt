package com.orbit.commandcenter.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orbit.commandcenter.R
import com.orbit.commandcenter.ui.components.OrbitInfoCard
import com.orbit.commandcenter.ui.components.OrbitMark
import com.orbit.commandcenter.ui.theme.OrbitTheme

// Hoisted so that scrolling this screen does not allocate padding objects per frame.
private val BackButtonPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
private val VersionNotePadding = Modifier.padding(top = 4.dp)

/**
 * Static "about" screen: explains what ORBIT is and what this build does not do yet.
 *
 * The content is a short, fixed set of cards, so it stays a plain scrolling [Column]:
 * every child is composed once and cached by the layout, which is cheaper than setting up
 * lazy-list machinery for five items.
 */
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TextButton(
                onClick = onNavigateBack,
                contentPadding = BackButtonPadding,
            ) {
                Text(
                    text = stringResource(R.string.about_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OrbitMark(modifier = Modifier.size(38.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.about_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Text(
                text = stringResource(R.string.about_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            OrbitInfoCard(
                title = stringResource(R.string.about_offline_title),
                body = stringResource(R.string.about_offline_body),
            )

            OrbitInfoCard(
                title = stringResource(R.string.about_stack_title),
                body = stringResource(R.string.about_stack_body),
            )

            Text(
                text = stringResource(
                    R.string.about_version_note,
                    stringResource(R.string.about_version_value),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = VersionNotePadding,
            )
        }
    }
}

@Preview(name = "ORBIT about (dark)", showBackground = true)
@Composable
private fun AboutScreenPreview() {
    OrbitTheme {
        AboutScreen(onNavigateBack = {})
    }
}
