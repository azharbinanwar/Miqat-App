package com.example.miqatapp.feature.tasbih.presentation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.widgets.LocalDrawerState

// Android Studio preview — tweak the ring geometry in BeadTasbihScreen and refresh here, no device build.
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
private fun BeadTasbihPreview() {
    AppTheme {
        CompositionLocalProvider(LocalDrawerState provides rememberDrawerState(DrawerValue.Closed)) {
            BeadTasbihScreen()
        }
    }
}
