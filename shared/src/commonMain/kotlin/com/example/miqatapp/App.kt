package com.example.miqatapp

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.config.theme.ThemeMode
import com.example.miqatapp.core.navigation.AppNavHost

@Composable
@Preview
fun App() {
    val systemDark = isSystemInDarkTheme()
    // ponytail: dev-only — long-press anywhere (empty area) flips light/dark to eyeball both. Not persisted.
    var override by remember { mutableStateOf<Boolean?>(null) }
    val dark = override ?: systemDark

    AppTheme(themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT) {
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onLongPress = { override = !dark }) }) {
            AppNavHost()
        }
    }
}
