package com.example.miqatapp.core.store

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One prayer's saved alert config — the subset the scheduler reads. Keyed by the prayer's name. */
data class AlertConfig(val enabled: Boolean, val atTime: Boolean, val remindBefore: Int)

/** Persists per-prayer notification settings. Screens read [configs] / [allAlerts]; mirrors PrayerFocusStore. */
object NotificationStore {
    private val keys = Miqat.PRAYERS.map { it.name }

    private val _allAlerts = MutableStateFlow(PrefsService.getBoolean(PrefConst.ALL_ALERTS, true))
    val allAlerts: StateFlow<Boolean> = _allAlerts.asStateFlow()

    private val _configs = MutableStateFlow(keys.associateWith { load(it) })
    val configs: StateFlow<Map<String, AlertConfig>> = _configs.asStateFlow()

    private fun load(prayer: String) = AlertConfig(
        enabled = PrefsService.getBoolean(PrefConst.alert(prayer, PrefConst.Field.ENABLED), false),
        atTime = PrefsService.getBoolean(PrefConst.alert(prayer, PrefConst.Field.AT_TIME), true),
        remindBefore = PrefsService.getInt(PrefConst.alert(prayer, PrefConst.Field.REMIND_BEFORE), 15),
    )

    fun setAllAlerts(value: Boolean) {
        PrefsService.putBoolean(PrefConst.ALL_ALERTS, value)
        _allAlerts.value = value
    }

    fun setEnabled(prayer: String, value: Boolean) {
        PrefsService.putBoolean(PrefConst.alert(prayer, PrefConst.Field.ENABLED), value)
        update(prayer) { it.copy(enabled = value) }
    }

    fun setAtTime(prayer: String, value: Boolean) {
        PrefsService.putBoolean(PrefConst.alert(prayer, PrefConst.Field.AT_TIME), value)
        update(prayer) { it.copy(atTime = value) }
    }

    fun setRemindBefore(prayer: String, value: Int) {
        PrefsService.putInt(PrefConst.alert(prayer, PrefConst.Field.REMIND_BEFORE), value)
        update(prayer) { it.copy(remindBefore = value) }
    }

    private fun update(prayer: String, f: (AlertConfig) -> AlertConfig) {
        _configs.value += (prayer to f(_configs.value.getValue(prayer)))
    }
}