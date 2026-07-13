package com.example.miqatapp.core.store

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.constants.defaults.FocusDefaults
import com.example.miqatapp.core.constants.defaults.FocusRow
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.focus.SilenceMode
import com.example.miqatapp.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One prayer's saved focus config. Keyed by [FocusDefaults] row keys. */
data class FocusConfig(val enabled: Boolean, val startAfter: Int, val duration: Int, val mode: SilenceMode)

/** Persists Prayer-Focus settings (per-field prefs), seeded from [FocusDefaults]. Screens read [configs]. */
object PrayerFocusStore {
    private val _configs = MutableStateFlow(FocusDefaults.rows.associate { it.key to load(it) })
    val configs: StateFlow<Map<String, FocusConfig>> = _configs.asStateFlow()

    private fun load(row: FocusRow) = FocusConfig(
        enabled = PrefsService.getBoolean(PrefConst.focus(row.key, PrefConst.Field.ENABLED), false),
        startAfter = PrefsService.getInt(PrefConst.focus(row.key, PrefConst.Field.START_AFTER), row.default.after),
        duration = PrefsService.getInt(PrefConst.focus(row.key, PrefConst.Field.DURATION), row.default.duration),
        mode = loadMode(row),
    )

    // Fajr defaults to Vibrate (you're asleep, still want to feel it); every other prayer defaults to Silent.
    private fun loadMode(row: FocusRow): SilenceMode {
        val default = if (row.miqat == Miqat.Fajr) SilenceMode.Vibrate else SilenceMode.Silent
        val saved = PrefsService.getString(PrefConst.focus(row.key, PrefConst.Field.SILENCE_MODE), default.name)
        return runCatching { SilenceMode.valueOf(saved) }.getOrDefault(default)
    }

    fun setEnabled(key: String, value: Boolean) {
        PrefsService.putBoolean(PrefConst.focus(key, PrefConst.Field.ENABLED), value)
        update(key) { it.copy(enabled = value) }
    }

    fun setStartAfter(key: String, value: Int) {
        PrefsService.putInt(PrefConst.focus(key, PrefConst.Field.START_AFTER), value)
        update(key) { it.copy(startAfter = value) }
    }

    fun setDuration(key: String, value: Int) {
        PrefsService.putInt(PrefConst.focus(key, PrefConst.Field.DURATION), value)
        update(key) { it.copy(duration = value) }
    }

    fun setMode(key: String, value: SilenceMode) {
        PrefsService.putString(PrefConst.focus(key, PrefConst.Field.SILENCE_MODE), value.name)
        update(key) { it.copy(mode = value) }
    }

    private fun update(key: String, f: (FocusConfig) -> FocusConfig) {
        _configs.value += (key to f(_configs.value.getValue(key)))
        // re-arm is driven reactively by FocusScheduling (watches configs), so no direct call here
    }
}
