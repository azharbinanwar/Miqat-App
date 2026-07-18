package com.example.miqatapp.feature.widget

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.enums.WidgetColor
import com.example.miqatapp.core.prefs.PrefsService

/** The Prayer Card's look (colour + opacity), one setting for every placed card. Edited in the Widgets screen. */
object WidgetConfig {
    fun opacity(): Float = PrefsService.getInt(PrefConst.WIDGET_CARD_OPACITY, 100).coerceIn(0, 100) / 100f
    fun color(): WidgetColor = WidgetColor.fromKey(PrefsService.getStringOrNull(PrefConst.WIDGET_CARD_COLOR))

    fun save(opacity: Float, color: WidgetColor) {
        PrefsService.putInt(PrefConst.WIDGET_CARD_OPACITY, (opacity * 100).toInt())
        PrefsService.putString(PrefConst.WIDGET_CARD_COLOR, color.key)
    }
}
