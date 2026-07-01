package com.example.miqatapp.core.prefs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings
import kotlin.reflect.KProperty

/**
 * The app's key-value preference store — one place to read/write everything, keyed by [PrefKeys].
 * Backed by multiplatform-settings (SharedPreferences on Android, NSUserDefaults on iOS). Typed accessors
 * are added per pref as they get wired; the generic passthroughs cover the rest.
 */
object Prefs {
    private val settings: Settings = Settings()

    /**
     * A String? pref that's also Compose state: reading it in a composable recomposes when it changes,
     * writing it persists. Enum-backed prefs store the enum's `value`; null clears the key (= follow system).
     */
    private class ReactivePref(private val settings: Settings, private val key: String) {
        private var state by mutableStateOf(settings.getStringOrNull(key))
        operator fun getValue(thisRef: Any?, p: KProperty<*>): String? = state
        operator fun setValue(thisRef: Any?, p: KProperty<*>, value: String?) {
            state = value
            if (value == null) settings.remove(key) else settings.putString(key, value)
        }
    }

    /** UI language: "en" | "ar", or null = follow system. */
    var language: String? by ReactivePref(settings, PrefKeys.LANGUAGE)

    /** Appearance: ThemeChoice.value ("Light" | "Dark" | "System"), or null = default. */
    var theme: String? by ReactivePref(settings, PrefKeys.THEME)

    /** Clock: TimeFormat.value ("Twelve" | "TwentyFour"), or null = default. */
    var timeFormat: String? by ReactivePref(settings, PrefKeys.TIME_FORMAT)

    /** The active city's display name (e.g. "Makkah"); null = the built-in default. ponytail: name only for now — full coords/tz record lands with the calc engine. */
    var activeCity: String? by ReactivePref(settings, PrefKeys.ACTIVE_CITY)

    /** Prayer calc: each stores the enum's `name` (CalculationMethod / Madhab / HighLatRule), null = default. */
    var calcMethod: String? by ReactivePref(settings, PrefKeys.CALC_METHOD)
    var madhab: String? by ReactivePref(settings, PrefKeys.MADHAB)
    var highLatRule: String? by ReactivePref(settings, PrefKeys.HIGH_LAT_RULE)

    // ── generic passthroughs (use a PrefKeys key) ────────────
    fun getString(key: String, default: String): String = settings.getString(key, default)
    fun getStringOrNull(key: String): String? = settings.getStringOrNull(key)
    fun putString(key: String, value: String) = settings.putString(key, value)
    fun getInt(key: String, default: Int): Int = settings.getInt(key, default)
    fun putInt(key: String, value: Int) = settings.putInt(key, value)
    fun getBoolean(key: String, default: Boolean): Boolean = settings.getBoolean(key, default)
    fun putBoolean(key: String, value: Boolean) = settings.putBoolean(key, value)
    fun remove(key: String) = settings.remove(key)
}
