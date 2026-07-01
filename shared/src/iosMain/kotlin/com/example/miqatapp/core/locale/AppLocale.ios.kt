package com.example.miqatapp.core.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual object LocalAppLocale {
    private const val LANG_KEY = "AppleLanguages"
    private val default: String = NSLocale.currentLocale.languageCode
    private val LocalAppLocale = staticCompositionLocalOf { default }

    actual val current: String
        @Composable @ReadOnlyComposable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val new = value ?: default
        val defaults = NSUserDefaults.standardUserDefaults
        if (value == null) defaults.removeObjectForKey(LANG_KEY) else defaults.setObject(listOf(value), LANG_KEY)
        return LocalAppLocale.provides(new)
    }
}
