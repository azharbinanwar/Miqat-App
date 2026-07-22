package com.example.miqatapp.feature.quran.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.config.theme.LocalAppColors
import com.example.miqatapp.feature.quran.data.QuranStore
import com.example.miqatapp.feature.quran.data.QuranTheme
import com.example.miqatapp.feature.quran.data.applyTo

// wraps the Quran index + reader in the picked reading theme: recolors the whole subtree, app stays untouched
@Composable
fun QuranThemeHost(content: @Composable () -> Unit) {
    val theme by QuranStore.theme.collectAsState()
    if (theme == QuranTheme.System) { content(); return } // follow the app theme — no override
    val colors = theme.applyTo(AppTheme.colors)
    // Material components (Scaffold, TopAppBar) read colorScheme, so mirror the same overrides there
    val scheme = MaterialTheme.colorScheme.copy(
        background = theme.background, onBackground = theme.onBackground,
        surface = theme.surface, onSurface = theme.onSurface,
        surfaceVariant = theme.surfaceContainer, onSurfaceVariant = theme.onSurfaceVariant,
        surfaceContainerLowest = theme.background, surfaceContainerLow = theme.surface,
        surfaceContainer = theme.surfaceContainer, surfaceContainerHigh = theme.surfaceContainer, surfaceContainerHighest = theme.surfaceContainer,
        outline = theme.outline, outlineVariant = theme.outline, surfaceTint = theme.primary,
        primary = theme.primary, onPrimary = theme.onPrimary,
    )
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
