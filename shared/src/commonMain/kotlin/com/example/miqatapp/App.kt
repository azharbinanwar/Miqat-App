package com.example.miqatapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.navigation.AppNavHost

@Composable
@Preview
fun App() {
    AppTheme {
        AppNavHost()
    }
}
