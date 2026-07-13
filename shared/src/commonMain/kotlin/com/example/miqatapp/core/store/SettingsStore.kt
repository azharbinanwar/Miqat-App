package com.example.miqatapp.core.store

import com.example.miqatapp.config.theme.ThemeChoice
import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.constants.defaults.SettingsDefaults
import com.example.miqatapp.core.datetime.HijriDate
import com.example.miqatapp.core.datetime.hijriToday
import com.example.miqatapp.core.locale.Language
import com.example.miqatapp.core.prefs.PrefsService
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.TimeFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    /** Today's Hijri date with the offset already applied — read this; the offset is the store's concern. */
    private val _hijriDate = MutableStateFlow(hijriToday(_hijriOffset.value))
    val hijriDate: StateFlow<HijriDate> = _hijriDate.asStateFlow()

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
        _hijriDate.value = hijriToday(value)
    }
}
