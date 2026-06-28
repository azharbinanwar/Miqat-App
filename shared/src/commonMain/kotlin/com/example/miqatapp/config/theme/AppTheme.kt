package com.example.miqatapp.config.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Theme selection — same modes as the Flutter theme cubit. Persist later via DataStore. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * App-wide theme. Wrap the app once with this (replaces Flutter's `AppTheme.light/.dark`).
 * Maps [AppColors] onto Material 3's ColorScheme AND exposes the extra colors via [AppTheme.colors].
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (dark) darkAppColors() else lightAppColors()

    val base = if (dark) darkColorScheme() else lightColorScheme()
    val scheme = base.copy(
        primary = colors.primary, onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryContainer, onPrimaryContainer = colors.onPrimaryContainer,
        inversePrimary = colors.inversePrimary,
        secondary = colors.secondary, onSecondary = colors.onSecondary,
        secondaryContainer = colors.secondaryContainer, onSecondaryContainer = colors.onSecondaryContainer,
        tertiary = colors.tertiary, onTertiary = colors.onTertiary,
        tertiaryContainer = colors.tertiaryContainer, onTertiaryContainer = colors.onTertiaryContainer,
        error = colors.error, onError = colors.onError,
        errorContainer = colors.errorContainer, onErrorContainer = colors.onErrorContainer,
        background = colors.background, onBackground = colors.onBackground,
        surface = colors.surface, onSurface = colors.onSurface,
        surfaceVariant = colors.surfaceVariant, onSurfaceVariant = colors.onSurfaceVariant,
        surfaceDim = colors.surfaceDim, surfaceBright = colors.surfaceBright,
        surfaceContainerLowest = colors.surfaceContainerLowest,
        surfaceContainerLow = colors.surfaceContainerLow,
        surfaceContainer = colors.surfaceContainer,
        surfaceContainerHigh = colors.surfaceContainerHigh,
        surfaceContainerHighest = colors.surfaceContainerHighest,
        inverseSurface = colors.inverseSurface, inverseOnSurface = colors.inverseOnSurface,
        outline = colors.outline, outlineVariant = colors.outlineVariant,
        surfaceTint = colors.surfaceTint, scrim = colors.scrim,
        primaryFixed = colors.primaryFixed, primaryFixedDim = colors.primaryFixedDim,
        onPrimaryFixed = colors.onPrimaryFixed, onPrimaryFixedVariant = colors.onPrimaryFixedVariant,
        secondaryFixed = colors.secondaryFixed, secondaryFixedDim = colors.secondaryFixedDim,
        onSecondaryFixed = colors.onSecondaryFixed, onSecondaryFixedVariant = colors.onSecondaryFixedVariant,
        tertiaryFixed = colors.tertiaryFixed, tertiaryFixedDim = colors.tertiaryFixedDim,
        onTertiaryFixed = colors.onTertiaryFixed, onTertiaryFixedVariant = colors.onTertiaryFixedVariant,
    )

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

/** Access the current palette anywhere under [AppTheme]: `AppTheme.colors.success`. */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}
