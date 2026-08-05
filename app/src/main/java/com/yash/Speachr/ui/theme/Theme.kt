package com.yash.Speachr.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.yash.Speachr.ui.theme.LocalSpeachrSpacing
import com.yash.Speachr.ui.theme.SpeachrSpacing

private val DarkColorScheme = darkColorScheme(
    primary = SpeachrPrimary,
    onPrimary = Color.White,
    primaryContainer = SpeachrOnPrimaryContainer,
    onPrimaryContainer = SpeachrPrimaryContainer,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color.White,
    onSurface = Color.White,
    error = SpeachrError
)

private val LightColorScheme = lightColorScheme(
    primary = SpeachrPrimary,
    onPrimary = Color.White,
    primaryContainer = SpeachrPrimaryContainer,
    onPrimaryContainer = SpeachrOnPrimaryContainer,
    background = SpeachrBackground,
    surface = SpeachrSurface,
    onBackground = SpeachrTextPrimary,
    onSurface = SpeachrTextPrimary,
    onSurfaceVariant = SpeachrTextSecondary,
    error = SpeachrError
)

object SpeachrTheme {
    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val spacing: SpeachrSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpeachrSpacing.current
}

@Composable
fun SpeachrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalSpeachrSpacing provides SpeachrSpacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
