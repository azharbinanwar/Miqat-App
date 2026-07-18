package com.example.miqatapp.feature.widget

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.enums.WidgetColor
import com.example.miqatapp.core.prefs.PrefsService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One placed widget's look. Stored as JSON per appWidgetId; unset fields fall back to these defaults. */
@Serializable
data class WidgetStyle(
    val colorKey: String = WidgetColor.default.key,
    val opacity: Int = 100, // 0..100
) {
    val color: WidgetColor get() = WidgetColor.fromKey(colorKey)
    val alpha: Float get() = opacity.coerceIn(0, 100) / 100f
}

/**
 * Per-instance widget style, keyed by appWidgetId — every placed widget owns its own look, nothing is shared.
 * A widget with no saved style just uses the neutral defaults. The gallery Customize sheet stashes its choice as
 * [PENDING]; the widget it drops adopts that onto its own id (via the configure activity, or [claim] as a fallback).
 */
object WidgetConfig {
    private const val PENDING = -1

    fun style(id: Int): WidgetStyle = parse(PrefsService.getStringOrNull(key(id))) ?: WidgetStyle()

    fun save(style: WidgetStyle, id: Int) { PrefsService.putString(key(id), Json.encodeToString(style)) }

    /** First render of a freshly-dropped widget adopts the pending look (if the configure step didn't), then owns it. */
    fun claim(id: Int): WidgetStyle {
        if (PrefsService.getStringOrNull(key(id)) == null) consumePendingInto(id)
        return style(id)
    }

    /** Gallery sheet stashes the chosen look; the dropped widget moves it onto its own id. */
    fun stashPending(style: WidgetStyle) = save(style, PENDING)

    fun pending(): WidgetStyle? = parse(PrefsService.getStringOrNull(key(PENDING)))

    fun clearPending() = PrefsService.remove(key(PENDING))

    fun consumePendingInto(id: Int) {
        val raw = PrefsService.getStringOrNull(key(PENDING)) ?: return
        PrefsService.putString(key(id), raw)
        PrefsService.remove(key(PENDING))
    }

    private fun key(id: Int) = "${PrefConst.WIDGET_STYLE}$id"

    private fun parse(json: String?): WidgetStyle? =
        json?.let { runCatching { Json.decodeFromString<WidgetStyle>(it) }.getOrNull() }
}
