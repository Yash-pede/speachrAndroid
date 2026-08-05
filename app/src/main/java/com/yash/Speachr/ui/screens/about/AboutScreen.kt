package com.yash.Speachr.ui.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.yash.Speachr.ui.theme.SpeachrTheme

@Composable
fun AboutScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpeachrTheme.spacing.large),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "About Speachr",
                style = SpeachrTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(SpeachrTheme.spacing.medium))
            Text(
                text = "Speachr is your companion for seamless AI-powered speech translation.",
                style = SpeachrTheme.typography.bodyLarge,
                color = SpeachrTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(SpeachrTheme.spacing.small))
            Text(
                text = "Version 1.0.0",
                style = SpeachrTheme.typography.labelLarge,
                color = SpeachrTheme.colors.primary
            )
        }
    }
}
