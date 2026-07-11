package com.example.miqatapp.feature.miqat.store

import com.example.miqatapp.core.constants.defaults.MiqatDefaults
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.prefs.PrefsService
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.feature.miqat.domain.MiqatCalculation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
object MiqatCalculationStore {

    private val _method = MutableStateFlow(
        CalculationMethod.fromName(PrefsService.getStringOrNull(PrefConst.CALC_METHOD))
            ?: CalculationMethod.forCountry(LocationStore.activePlace.value.countryCode),
    )
    val method: StateFlow<CalculationMethod> = _method.asStateFlow()

    private val _madhab = MutableStateFlow(Madhab.fromName(PrefsService.getStringOrNull(PrefConst.MADHAB)) ?: MiqatDefaults.madhab)
    val madhab: StateFlow<Madhab> = _madhab.asStateFlow()

    private val _highLatRule = MutableStateFlow(HighLatRule.fromName(PrefsService.getStringOrNull(PrefConst.HIGH_LAT_RULE)) ?: MiqatDefaults.highLatRule)
    val highLatRule: StateFlow<HighLatRule> = _highLatRule.asStateFlow()

    private val _fajrAngle = MutableStateFlow(PrefsService.getInt(PrefConst.CUSTOM_FAJR_ANGLE, MiqatDefaults.FAJR_ANGLE))
    val fajrAngle: StateFlow<Int> = _fajrAngle.asStateFlow()

    private val _ishaAngle = MutableStateFlow(PrefsService.getInt(PrefConst.CUSTOM_ISHA_ANGLE, MiqatDefaults.ISHA_ANGLE))
    val ishaAngle: StateFlow<Int> = _ishaAngle.asStateFlow()

    /** Per-prayer ± minute tweak, keyed by the six daily Miqats; missing = default (0). */
    private val _adjustments = MutableStateFlow(
        Miqat.DAILY.associateWith { PrefsService.getInt(PrefConst.adjust(it.name), MiqatDefaults.MINUTE_ADJUSTMENT) },
    )
    val adjustments: StateFlow<Map<Miqat, Int>> = _adjustments.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** All settings as one observable DTO — the reactive input the times store watches. */
    val calculation: StateFlow<MiqatCalculation> = combine(
        combine(method, madhab, highLatRule) { m, md, h -> Triple(m, md, h) },
        combine(fajrAngle, ishaAngle, adjustments) { f, i, a -> Triple(f, i, a) },
    ) { core, ang ->
        MiqatCalculation(core.first, core.second, core.third, ang.first, ang.second, ang.third)
    }.stateIn(scope, SharingStarted.Eagerly, snapshot())

    // ── setters: persist + emit (the only way to write these settings) ──

    fun setMethod(value: CalculationMethod) {
        PrefsService.putString(PrefConst.CALC_METHOD, value.name)
        _method.value = value
    }

    fun setMadhab(value: Madhab) {
        PrefsService.putString(PrefConst.MADHAB, value.name)
        _madhab.value = value
    }

    fun setHighLatRule(value: HighLatRule) {
        PrefsService.putString(PrefConst.HIGH_LAT_RULE, value.name)
        _highLatRule.value = value
    }

    fun setFajrAngle(degrees: Int) {
        PrefsService.putInt(PrefConst.CUSTOM_FAJR_ANGLE, degrees)
        _fajrAngle.value = degrees
    }

    fun setIshaAngle(degrees: Int) {
        PrefsService.putInt(PrefConst.CUSTOM_ISHA_ANGLE, degrees)
        _ishaAngle.value = degrees
    }

    fun setAdjustment(miqat: Miqat, minutes: Int) {
        PrefsService.putInt(PrefConst.adjust(miqat.name), minutes)
        _adjustments.value += (miqat to minutes)
    }

    /** Freeze the current settings into an immutable [MiqatCalculation] — the engine's input DTO. */
    fun snapshot() = MiqatCalculation(
        method = _method.value,
        madhab = _madhab.value,
        highLatRule = _highLatRule.value,
        fajrAngle = _fajrAngle.value,
        ishaAngle = _ishaAngle.value,
        adjustments = _adjustments.value,
    )
}
