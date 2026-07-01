package com.example.miqatapp.core.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Platform glue that re-points Compose Resources at a language at runtime (so a language switch re-renders
 * without an app restart). Provide it around the app and `key()` on the language to force recomposition.
 * The chosen language itself lives in [com.example.miqatapp.core.prefs.Prefs.language] (reactive + persisted).
 */
expect object LocalAppLocale {
    val current: String
        @Composable @ReadOnlyComposable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
