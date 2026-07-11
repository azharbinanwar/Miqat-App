package com.example.miqatapp.core.store

import com.example.miqatapp.config.theme.ThemeChoice
import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.constants.defaults.SettingsDefaults
import com.example.miqatapp.core.locale.Language
import com.example.miqatapp.core.prefs.PrefsService
import com.example.miqatapp.core.prefs.TimeFormat
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

    private val _hijriOffset = MutableStateFlow(PrefsService.getInt(PrefConst.HIJRI_OFFSET, SettingsDefaults.HIJRI_OFFSET))
    val hijriOffset: StateFlow<Int> = _hijriOffset.asStateFlow()

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

    fun setHijriOffset(value: Int) {
        PrefsService.putInt(PrefConst.HIJRI_OFFSET, value)
        _hijriOffset.value = value
    }
}
