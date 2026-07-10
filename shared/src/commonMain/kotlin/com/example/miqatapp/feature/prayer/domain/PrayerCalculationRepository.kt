package com.example.miqatapp.feature.prayer.domain

import com.example.miqatapp.core.constants.MiqatDefaults
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.prefs.PrefKeys
import com.example.miqatapp.core.prefs.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for prayer-calculation settings. Owns the only `Prefs ?: MiqatDefaults`
 * resolution in the app — callers read the flows and always get a resolved value (never null, never
 * a raw default lookup).
 *
 * Reads Prefs ONCE (to seed each flow at startup); after that every consumer reads the flow. Setters
 * do both jobs — persist to Prefs AND push the new value into the flow, so any writer (Settings, Home)
 * updates every observer (screens now; the engine/notifications later).
 *
 * Plain `object` = one app-wide instance. Screens observe via `collectAsState()`; the engine reads `.value`.
 */
object PrayerCalculationRepository {

    private val _method = MutableStateFlow(CalculationMethod.fromName(Prefs.calcMethod))
    val method: StateFlow<CalculationMethod> = _method.asStateFlow()

    private val _madhab = MutableStateFlow(Madhab.fromName(Prefs.madhab))
    val madhab: StateFlow<Madhab> = _madhab.asStateFlow()

    private val _highLatRule = MutableStateFlow(HighLatRule.fromName(Prefs.highLatRule))
    val highLatRule: StateFlow<HighLatRule> = _highLatRule.asStateFlow()

    private val _fajrAngle = MutableStateFlow(Prefs.getInt(PrefKeys.CUSTOM_FAJR_ANGLE, MiqatDefaults.FAJR_ANGLE))
    val fajrAngle: StateFlow<Int> = _fajrAngle.asStateFlow()

    private val _ishaAngle = MutableStateFlow(Prefs.getInt(PrefKeys.CUSTOM_ISHA_ANGLE, MiqatDefaults.ISHA_ANGLE))
    val ishaAngle: StateFlow<Int> = _ishaAngle.asStateFlow()

    /** Per-prayer ± minute tweak, keyed by the six daily Miqats; missing = default (0). */
    private val _adjustments = MutableStateFlow(
        Miqat.DAILY.associateWith { Prefs.getInt(PrefKeys.adjust(it.name), MiqatDefaults.MINUTE_ADJUSTMENT) },
    )
    val adjustments: StateFlow<Map<Miqat, Int>> = _adjustments.asStateFlow()

    // ── setters: persist + emit (the only way to write these settings) ──

    fun setMethod(value: CalculationMethod) {
        Prefs.calcMethod = value.name
        _method.value = value
    }

    fun setMadhab(value: Madhab) {
        Prefs.madhab = value.name
        _madhab.value = value
    }

    fun setHighLatRule(value: HighLatRule) {
        Prefs.highLatRule = value.name
        _highLatRule.value = value
    }

    fun setFajrAngle(degrees: Int) {
        Prefs.putInt(PrefKeys.CUSTOM_FAJR_ANGLE, degrees)
        _fajrAngle.value = degrees
    }

    fun setIshaAngle(degrees: Int) {
        Prefs.putInt(PrefKeys.CUSTOM_ISHA_ANGLE, degrees)
        _ishaAngle.value = degrees
    }

    fun setAdjustment(miqat: Miqat, minutes: Int) {
        Prefs.putInt(PrefKeys.adjust(miqat.name), minutes)
        _adjustments.value = _adjustments.value + (miqat to minutes)
    }
}
