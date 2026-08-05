package com.yash.Speachr.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yash.Speachr.ui.theme.SpeachrTheme

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpeachrTheme.spacing.large),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Settings",
                style = SpeachrTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(SpeachrTheme.spacing.medium))
            Text(
                text = "Account, Notifications, and Language options will appear here.",
                style = SpeachrTheme.typography.bodyLarge,
                color = SpeachrTheme.colors.onSurfaceVariant
            )
        }
    }
}
