package com.example.miqatapp.core.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable

// Switches the UI language at runtime, no app restart.
expect object LocalAppLocale {
    val current: String
        @Composable @ReadOnlyComposable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
