package com.example.miqatapp.core.store

import com.example.miqatapp.config.theme.ThemeChoice
import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.constants.defaults.SettingsDefaults
import com.example.miqatapp.core.datetime.HijriDate
import com.example.miqatapp.core.datetime.Now
import com.example.miqatapp.core.locale.Language
import com.example.miqatapp.core.prefs.PrefsService
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.TimeFormat
import com.example.miqatapp.core.datetime.toHijri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

/**
 * General app settings — theme, language, clock format, Hijri offset. Same shape as the other stores:
 * seed each flow from [PrefsService] once (`getX ?: SettingsDefaults`), then consumers observe the flow;
 * setters persist via the generic key-value passthroughs AND emit, so a change anywhere updates everyone.
 */
object SettingsStore {

    private val _theme = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.THEME)?.let { ThemeChoice.fromValue(it) } ?: SettingsDefaults.theme,
    )
    val theme: StateFlow<ThemeChoice> = _theme.asStateFlow()

    private val _language = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.LANGUAGE)?.let { Language.fromCode(it) } ?: SettingsDefaults.language,
    )
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _timeFormat = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.TIME_FORMAT)?.let { TimeFormat.fromValue(it) } ?: SettingsDefaults.timeFormat,
    )
    val timeFormat: StateFlow<TimeFormat> = _timeFormat.asStateFlow()

    /** Which time the Ramadan "Sehri" label follows — Fajr (the ruling) or Imsak (the precaution). */
    private val _sehriReference = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.SEHRI_REFERENCE)?.let { name -> Miqat.entries.firstOrNull { it.name == name } } ?: SettingsDefaults.sehriReference,
    )
    val sehriReference: StateFlow<Miqat> = _sehriReference.asStateFlow()

    private val _hijriOffset = MutableStateFlow(PrefsService.getInt(PrefConst.HIJRI_OFFSET, SettingsDefaults.HIJRI_OFFSET))
    val hijriOffset: StateFlow<Int> = _hijriOffset.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Today's Hijri date with the offset applied; follows Now, so it rolls with the clock. */
    val hijriDate: StateFlow<HijriDate> = combine(
        Now.now.map { it.date }.distinctUntilChanged(),
        _hijriOffset,
    ) { date, off -> toHijri(date.plus(off, DateTimeUnit.DAY)) }
        .stateIn(scope, SharingStarted.Eagerly, Now.hijri(_hijriOffset.value))

    fun setTheme(value: ThemeChoice) {
        PrefsService.putString(PrefConst.THEME, value.value)
        _theme.value = value
    }

    fun setLanguage(value: Language) {
        PrefsService.putString(PrefConst.LANGUAGE, value.code)
        _language.value = value
    }

    fun setTimeFormat(value: TimeFormat) {
        PrefsService.putString(PrefConst.TIME_FORMAT, value.value)
        _timeFormat.value = value
    }

    fun setSehriReference(value: Miqat) {
        PrefsService.putString(PrefConst.SEHRI_REFERENCE, value.name)
        _sehriReference.value = value
    }

    fun setHijriOffset(value: Int) {
        PrefsService.putInt(PrefConst.HIJRI_OFFSET, value)
        _hijriOffset.value = value
    }
}
