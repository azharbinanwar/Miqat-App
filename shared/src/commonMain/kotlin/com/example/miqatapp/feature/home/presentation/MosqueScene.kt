package com.example.miqatapp.feature.home.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.feature.miqat.domain.MiqatTime
import kotlinx.datetime.LocalTime
import kotlin.math.PI
import kotlin.math.sin

/** A fixed anchor look for a prayer: sky palette, night depth, and where its orb sits on the arc. */
private data class Scene(val arcPos: Float, val sky: List<Color>, val moon: Boolean, val night: Float)

private fun sceneFor(p: Miqat): Scene = when (p) {
    Miqat.Fajr -> Scene(0.10f, listOf(Color(0xFF1A1740), Color(0xFF3B2E63), Color(0xFF7A5A86)), moon = true, night = 0.70f)
    Miqat.Sunrise -> Scene(0.15f, listOf(Color(0xFF2A4A8A), Color(0xFFB85C73), Color(0xFFF0A85C)), moon = false, night = 0.18f)
    Miqat.Dhuhr -> Scene(0.50f, listOf(Color(0xFF2E83C9), Color(0xFF6FB6E8), Color(0xFFC6E6FB)), moon = false, night = 0f)
    Miqat.Asr -> Scene(0.80f, listOf(Color(0xFF3E78B0), Color(0xFF8FB4D8), Color(0xFFE6D6A8)), moon = false, night = 0.08f)
    Miqat.Maghrib -> Scene(0.94f, listOf(Color(0xFF2B1E55), Color(0xFF8E3A63), Color(0xFFE8843C)), moon = false, night = 0.42f)
    Miqat.Isha -> Scene(0.55f, listOf(Color(0xFF050912), Color(0xFF0E1430), Color(0xFF221A45)), moon = true, night = 0.95f)
    Miqat.Imsak -> sceneFor(Miqat.Fajr)
    Miqat.Ishraq, Miqat.Zawal -> sceneFor(Miqat.Dhuhr)
    Miqat.Sunset -> sceneFor(Miqat.Maghrib)
    Miqat.Midnight, Miqat.LastThird -> sceneFor(Miqat.Isha)
}

/**
 * The live sky the scene draws. Sun and moon are two independent bodies (x + altitude); `alt < -0.5`
 * means "hidden" (below the horizon / out of frame). They live on opposite arcs so they never overlap.
 */
data class SkyState(
    val sky: List<Color>,
    val night: Float,
    val sunX: Float,
    val sunAlt: Float,
    val moonX: Float,
    val moonAlt: Float,
)

private const val HIDDEN = -1f   // altitude sentinel: body is fully below the horizon / off-frame

private val DAY_ORDER = listOf(Miqat.Fajr, Miqat.Sunrise, Miqat.Dhuhr, Miqat.Asr, Miqat.Maghrib, Miqat.Isha)

private class AnchorPt(val min: Int, val scene: Scene)
private class Slot(val a: Scene, val aMin: Int, val b: Scene, val bMin: Int)
private class Body(val x: Float, val alt: Float)

/**
 * Where the sky, sun and moon actually are right now.
 *  - Sky palette + night depth: interpolated between the surrounding prayer anchors (the same look as before).
 *  - Sun: rides the DAY arc (Sunrise→Maghrib). Rises east behind the mountains, peaks at Dhuhr, sets west.
 *  - Moon: rides the NIGHT arc (Maghrib→next Sunrise). Rises east as the sun sets west, sets by dawn.
 * Each body's ends dip below the horizon so the mountains (painted on top) swallow it — a real rise/set,
 * no fade. Because they're on opposite arcs, at dawn/dusk they sit at opposite horizons and never overlap.
 */
fun liveSkyState(now: LocalTime, times: List<MiqatTime>): SkyState {
    fun minOf(m: Miqat): Int? = times.firstOrNull { it.miqat == m }?.let { it.at.time.hour * 60 + it.at.time.minute }
    val n = now.hour * 60 + now.minute

    // sky palette + night depth — interpolate the surrounding prayer anchors
    val anchors = DAY_ORDER.mapNotNull { m -> minOf(m)?.let { AnchorPt(it, sceneFor(m)) } }.sortedBy { it.min }
    val sky: List<Color>
    val night: Float
    if (anchors.size < 2) {
        sceneFor(Miqat.Dhuhr).let { sky = it.sky; night = it.night }
    } else {
        val first = anchors.first(); val last = anchors.last()
        val slot = when {
            n < first.min -> Slot(last.scene, last.min - 1440, first.scene, first.min)
            n >= last.min -> Slot(last.scene, last.min, first.scene, first.min + 1440)
            else -> { val i = anchors.indexOfLast { it.min <= n }; Slot(anchors[i].scene, anchors[i].min, anchors[i + 1].scene, anchors[i + 1].min) }
        }
        val p = ((n - slot.aMin).toFloat() / (slot.bMin - slot.aMin)).coerceIn(0f, 1f)
        sky = listOf(
            lerp(slot.a.sky[0], slot.b.sky[0], p),
            lerp(slot.a.sky[1], slot.b.sky[1], p),
            lerp(slot.a.sky[2], slot.b.sky[2], p),
        )
        night = lerp(slot.a.night, slot.b.night, p)
    }

    // sun & moon as two bodies. buf = fraction of the arc spent dipping below the horizon at each end.
    val sunrise = minOf(Miqat.Sunrise) ?: (6 * 60)
    val maghrib = minOf(Miqat.Maghrib) ?: (18 * 60)
    val dayLen = (maghrib - sunrise).coerceAtLeast(1)
    val nightLen = (sunrise + 1440 - maghrib).coerceAtLeast(1)
    val buf = 0.06f

    val dp = (n - sunrise).toFloat() / dayLen                       // day progress, <0 or >1 in the buffer
    val sun = if (dp > -buf && dp < 1f + buf) Body(0.12f + 0.76f * dp.coerceIn(0f, 1f), sin(dp * PI).toFloat())
    else Body(0.5f, HIDDEN)

    val nAdj = if (n >= maghrib) n else n + 1440                    // early morning belongs to the previous night
    val qp = (nAdj - maghrib).toFloat() / nightLen                 // night progress
    val moon = if (qp > -buf && qp < 1f + buf) Body(0.12f + 0.76f * qp.coerceIn(0f, 1f), sin(qp * PI).toFloat())
    else Body(0.5f, HIDDEN)

    return SkyState(sky, night, sun.x, sun.alt, moon.x, moon.alt)
}

private val starField = listOf(
    0.10f to 0.22f, 0.20f to 0.46f, 0.34f to 0.16f, 0.48f to 0.36f, 0.60f to 0.24f,
    0.72f to 0.42f, 0.84f to 0.18f, 0.92f to 0.52f, 0.16f to 0.62f, 0.44f to 0.60f,
    0.68f to 0.56f, 0.88f to 0.66f,
)

/**
 * Reusable mosque + mountains scene for a live [state]. The sun and moon are drawn *before* the mountains,
 * so any body below the horizon is occluded by them — it rises from and sets behind the peaks.
 */
@Composable
fun MosqueScene(state: SkyState, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val topInsetPx = WindowInsets.statusBars.getTop(density).toFloat()
    val night by animateFloatAsState(state.night, tween(700), label = "night")
    val sunX by animateFloatAsState(state.sunX, tween(500), label = "sunX")
    val sunAlt by animateFloatAsState(state.sunAlt, tween(500), label = "sunAlt")
    val moonX by animateFloatAsState(state.moonX, tween(500), label = "moonX")
    val moonAlt by animateFloatAsState(state.moonAlt, tween(500), label = "moonAlt")
    val c0 by animateColorAsState(state.sky[0], tween(700), label = "c0")
    val c1 by animateColorAsState(state.sky[1], tween(700), label = "c1")
    val c2 by animateColorAsState(state.sky[2], tween(700), label = "c2")
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        0.20f, 0.50f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "glow",
    )

    val sunColor = Color(0xFFFFD54F)
    val moonColor = Color(0xFFE8EAF6)
    val baseMtn = lerp(Color(0xFF1A2A3A), Color(0xFF070B16), night)

    Box(modifier.background(Brush.verticalGradient(listOf(c0, c1, c2)))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val horizonY = h * 0.66f
            val topLimit = topInsetPx + 64.dp.toPx()
            val amp = (horizonY - topLimit).coerceAtLeast(1f)

            // stars (night only)
            if (night > 0.05f) {
                starField.forEach { (xf, yf) ->
                    drawCircle(Color.White.copy(alpha = night * 0.7f), 1.6.dp.toPx(), Offset(xf * w, yf * horizonY))
                }
            }

            // sun — only while near/above the horizon (its ends dip under, then the mountains hide it)
            if (sunAlt > -0.5f) {
                val ox = sunX * w
                val oy = horizonY - amp * sunAlt
                drawCircle(sunColor.copy(alpha = glow * 0.5f), 56.dp.toPx(), Offset(ox, oy))
                drawCircle(sunColor.copy(alpha = glow), 36.dp.toPx(), Offset(ox, oy))
                drawCircle(sunColor, 24.dp.toPx(), Offset(ox, oy))
            }

            // moon — crescent carved with the sky color at its height (no glow → no eclipse look)
            if (moonAlt > -0.5f) {
                val ox = moonX * w
                val oy = horizonY - amp * moonAlt
                val frac = (oy / h).coerceIn(0f, 1f)
                val skyHere = if (frac < 0.5f) lerp(c0, c1, frac * 2f) else lerp(c1, c2, (frac - 0.5f) * 2f)
                drawCircle(moonColor, 22.dp.toPx(), Offset(ox, oy))
                drawCircle(skyHere, 19.dp.toPx(), Offset(ox + 9.dp.toPx(), oy - 6.dp.toPx()))
            }

            // multi-shade layered mountains (back hazy → front dark) — painted OVER the orbs
            val backColor = lerp(baseMtn, c2, 0.45f)
            val midColor = baseMtn
            val frontColor = lerp(baseMtn, Color.Black, 0.35f)

            drawPath(Path().apply {
                moveTo(0f, h); lineTo(0f, horizonY + h * 0.03f)
                lineTo(w * 0.18f, horizonY - h * 0.04f); lineTo(w * 0.38f, horizonY + h * 0.04f)
                lineTo(w * 0.60f, horizonY - h * 0.05f); lineTo(w * 0.82f, horizonY + h * 0.02f)
                lineTo(w, horizonY - h * 0.02f); lineTo(w, h); close()
            }, backColor)

            drawPath(Path().apply {
                moveTo(0f, h); lineTo(0f, horizonY + h * 0.09f)
                lineTo(w * 0.25f, horizonY + h * 0.02f); lineTo(w * 0.50f, horizonY + h * 0.10f)
                lineTo(w * 0.72f, horizonY + h * 0.01f); lineTo(w, horizonY + h * 0.07f); lineTo(w, h); close()
            }, midColor)

            drawPath(Path().apply {
                moveTo(0f, h); lineTo(0f, horizonY + h * 0.16f)
                lineTo(w * 0.30f, horizonY + h * 0.08f); lineTo(w * 0.55f, horizonY + h * 0.17f)
                lineTo(w * 0.80f, horizonY + h * 0.09f); lineTo(w, horizonY + h * 0.15f); lineTo(w, h); close()
            }, frontColor)
        }
    }
}
