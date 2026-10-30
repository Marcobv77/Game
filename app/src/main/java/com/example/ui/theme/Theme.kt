package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EclipseDarkColorScheme = darkColorScheme(
    primary = CoronaAmber,
    onPrimary = VoidBlack,
    primaryContainer = CoronaAmberDim,
    onPrimaryContainer = EclipseGold,
    secondary = TelemetryCyan,
    onSecondary = VoidBlack,
    secondaryContainer = TelemetryCyanDim,
    onSecondaryContainer = TerminalText,
    tertiary = OxygenTeal,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = TerminalText,
    surface = CockpitSurface,
    onSurface = TerminalText,
    surfaceVariant = CockpitSurfaceVariant,
    onSurfaceVariant = TerminalTextMuted,
    error = HazardRed,
    onError = VoidBlack,
    outline = CardBorderGlow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Eclipse Drift is fundamentally a deep-space game
    dynamicColor: Boolean = false, // Keep consistent hard sci-fi color palette
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = EclipseDarkColorScheme,
        typography = Typography,
        content = content
    )
}
