package com.example.miqatapp.feature.quran.data

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.constants.defaults.QuranDefaults
import com.example.miqatapp.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Quran reader settings (feature-local): each flow seeded from PrefsService, setters persist + emit
object QuranStore {

    private val _fontSize = MutableStateFlow(PrefsService.getInt(PrefConst.QURAN_FONT_SP, QuranDefaults.FONT_SP))
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()

    fun setFontSize(value: Int) {
        PrefsService.putInt(PrefConst.QURAN_FONT_SP, value)
        _fontSize.value = value
    }

    private val _font = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.QURAN_FONT)
            ?.let { runCatching { QuranFont.valueOf(it) }.getOrNull() } ?: QuranFont.DEFAULT,
    )
    val font: StateFlow<QuranFont> = _font.asStateFlow()

    fun setFont(value: QuranFont) {
        PrefsService.putString(PrefConst.QURAN_FONT, value.name)
        _font.value = value
    }

    private val _theme = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.QURAN_THEME)
            ?.let { runCatching { QuranTheme.valueOf(it) }.getOrNull() } ?: QuranTheme.DEFAULT,
    )
    val theme: StateFlow<QuranTheme> = _theme.asStateFlow()

    fun setTheme(value: QuranTheme) {
        PrefsService.putString(PrefConst.QURAN_THEME, value.name)
        _theme.value = value
    }

    private val _favorites = MutableStateFlow(
        PrefsService.getStringOrNull(PrefConst.QURAN_FAVORITES)
            ?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet(),
    )
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    fun toggleFavorite(surah: Int) {
        val next = _favorites.value.toMutableSet().apply { if (!add(surah)) remove(surah) }
        PrefsService.putString(PrefConst.QURAN_FAVORITES, next.joinToString(","))
        _favorites.value = next
    }
}
