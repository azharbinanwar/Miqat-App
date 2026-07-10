package com.example.miqatapp.core.prefs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.miqatapp.core.constants.Place
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty

/**
 * The app's key-value preference store — one place to read/write everything, keyed by [PrefKeys].
 * Backed by multiplatform-settings (SharedPreferences on Android, NSUserDefaults on iOS). Typed accessors
 * are added per pref as they get wired; the generic passthroughs cover the rest.
 */
object Prefs {
    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

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

    /** The selected location as JSON; null = none saved (engine falls back to MiqatDefaults.place). Reactive. */
    private var activePlaceRaw: String? by ReactivePref(settings, PrefKeys.ACTIVE_PLACE)
    var activePlace: Place?
        get() = activePlaceRaw?.let { runCatching { json.decodeFromString<Place>(it) }.getOrNull() }
        set(value) { activePlaceRaw = value?.let { json.encodeToString(it) } }

    /** Saved locations (favorites) as a JSON array; empty when none. Reactive. */
    private var savedPlacesRaw: String? by ReactivePref(settings, PrefKeys.SAVED_PLACES)
    var savedPlaces: List<Place>
        get() = savedPlacesRaw?.let { runCatching { json.decodeFromString<List<Place>>(it) }.getOrNull() } ?: emptyList()
        set(value) { savedPlacesRaw = if (value.isEmpty()) null else json.encodeToString(value) }

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
