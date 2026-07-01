package com.example.miqatapp.core.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null
    private val LocalAppLocale = staticCompositionLocalOf { Locale.getDefault().toString() }

    actual val current: String
        @Composable @ReadOnlyComposable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current
        if (default == null) default = Locale.getDefault()
        val new = if (value == null) default!! else Locale(value)
        Locale.setDefault(new)
        configuration.setLocale(new)
        val resources = LocalContext.current.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalAppLocale.provides(new.toString())
    }
}
