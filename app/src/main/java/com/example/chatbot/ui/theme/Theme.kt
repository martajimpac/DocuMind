package com.example.chatbot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Fixed light palette: the design has no dark variant and must not follow the wallpaper (CA-01, CA-12).
private val DocuMindColorScheme = lightColorScheme(
    primary = Violet,
    onPrimary = White,
    primaryContainer = VioletTint,
    onPrimaryContainer = Violet,
    secondary = Violet,
    onSecondary = White,
    secondaryContainer = VioletTint,
    onSecondaryContainer = Violet,
    background = Cream,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = Cream,
    onSurfaceVariant = Muted,
    surfaceContainer = White,
    surfaceContainerLow = White,
    surfaceContainerHigh = White,
    surfaceContainerHighest = Cream,
    outline = Hairline,
    outlineVariant = Hairline,
    error = Danger,
    onError = White,
)

/** Colors with no Material 3 slot. */
@Immutable
data class DocuMindColors(
    val success: Color = Success,
    val sheetHandle: Color = Handle,
    val shadow: Color = ShadowTint,
)

private val LocalDocuMindColors = staticCompositionLocalOf { DocuMindColors() }
private val LocalDocuMindTypography = staticCompositionLocalOf { DocuMindTypography() }
private val LocalDocuMindShapes = staticCompositionLocalOf { DocuMindShapes() }

/** Access to the design tokens that Material 3 does not cover. */
object DocuMindTheme {
    val colors: DocuMindColors
        @Composable @ReadOnlyComposable get() = LocalDocuMindColors.current
    val typography: DocuMindTypography
        @Composable @ReadOnlyComposable get() = LocalDocuMindTypography.current
    val shapes: DocuMindShapes
        @Composable @ReadOnlyComposable get() = LocalDocuMindShapes.current
}

@Composable
fun ChatbotTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalDocuMindColors provides DocuMindColors(),
        LocalDocuMindTypography provides DocuMindTypography(),
        LocalDocuMindShapes provides DocuMindShapes(),
    ) {
        MaterialTheme(
            colorScheme = DocuMindColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
