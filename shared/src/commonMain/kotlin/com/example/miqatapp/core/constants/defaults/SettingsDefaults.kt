package com.example.miqatapp.core.constants.defaults

import com.example.miqatapp.config.theme.ThemeChoice
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.locale.Language
import com.example.miqatapp.core.enums.TimeFormat

/**
 * Ship defaults for the general app settings — the values a new user gets before touching Settings.
 * Read only by `SettingsStore`, as the fallback when `PrefsService` has no saved value. Mirrors [MiqatDefaults].
 */
object SettingsDefaults {
    val theme = ThemeChoice.default          // System
    val language = Language.English          // fromCode(null) resolves here
    val timeFormat = TimeFormat.default      // 12-hour
    const val HIJRI_OFFSET = 0
    val sehriReference = Miqat.Imsak         // cautious default; user can switch to Fajr in Ramadan
}
