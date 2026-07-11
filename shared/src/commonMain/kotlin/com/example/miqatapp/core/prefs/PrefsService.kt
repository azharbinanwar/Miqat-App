package com.example.miqatapp.core.prefs

import com.russhwolf.settings.Settings

/**
 * Dumb key-value storage over multiplatform-settings (SharedPreferences on Android, NSUserDefaults on iOS).
 * Nothing app-specific lives here — keys are in [com.example.miqatapp.core.constants.PrefConst], and all
 * typing, defaults, parsing and reactivity live in the stores. Add a setting → it's just another key,
 * this file never grows.
 */
object PrefsService {
    private val settings: Settings = Settings()

    fun getString(key: String, default: String): String = settings.getString(key, default)
    fun getStringOrNull(key: String): String? = settings.getStringOrNull(key)
    fun putString(key: String, value: String) = settings.putString(key, value)
    fun getInt(key: String, default: Int): Int = settings.getInt(key, default)
    fun putInt(key: String, value: Int) = settings.putInt(key, value)
    fun getBoolean(key: String, default: Boolean): Boolean = settings.getBoolean(key, default)
    fun putBoolean(key: String, value: Boolean) = settings.putBoolean(key, value)
    fun remove(key: String) = settings.remove(key)
}
