package com.example.miqatapp

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.config.theme.ThemeChoice
import com.example.miqatapp.config.theme.ThemeMode
import com.example.miqatapp.core.locale.Language
import com.example.miqatapp.core.locale.LocalAppLocale
import com.example.miqatapp.core.navigation.AppNavHost
import com.example.miqatapp.core.prefs.Prefs

@Composable
@Preview
fun App() {
    val systemDark = isSystemInDarkTheme()
    // ponytail: dev-only — long-press anywhere (empty area) flips light/dark to eyeball both. Not persisted.
    var override by remember { mutableStateOf<Boolean?>(null) }
    // override (dev long-press) wins, else the saved theme, else the system; System theme's dark == null falls through
    val dark = override ?: ThemeChoice.fromValue(Prefs.theme).dark ?: systemDark
    val language = Language.fromCode(Prefs.language)

    // re-point Compose Resources at the chosen language; key() forces a re-render on switch, RTL comes off the enum
    CompositionLocalProvider(
        LocalAppLocale provides language.code,
        LocalLayoutDirection provides language.direction,
    ) {
        key(language.code) {
            AppTheme(themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT) {
                Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onLongPress = { override = !dark }) }) {
                    AppNavHost()
                }
            }
        }
    }
}
