package com.example.miqatapp.feature.qibla.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Old classical Qibla dial — a mariner's compass rose: 8-point two-tone star,
 * double degree ring, ornate center. Fully theme-driven. Glows when [aligned].
 */
@Composable
fun QiblaDialClassical(
    headingDeg: Float,
    qiblaDeg: Float,
    aligned: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val tm = rememberTextMeasurer()

    val roseRot by animateFloatAsState(-headingDeg, tween(180), label = "rose")
    val needleRot by animateFloatAsState(qiblaDeg - headingDeg, tween(180), label = "needle")

    val pulse = rememberInfiniteTransition(label = "pulse")
    val glow by pulse.animateFloat(0.16f, 0.40f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "glow")

    val mark = if (aligned) c.success else c.primary

    Canvas(modifier.fillMaxWidth().aspectRatio(1f)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f * 0.88f

        fun polar(deg: Float, radius: Float): Offset {
            val a = (deg - 90f) * (PI / 180f).toFloat()
            return Offset(center.x + cos(a) * radius, center.y + sin(a) * radius)
        }

        // shadow + face
        drawCircle(c.onSurface.copy(alpha = 0.10f), radius = r, center = center + Offset(0f, 5.dp.toPx()))
        if (aligned) {
            drawCircle(c.success.copy(alpha = glow), radius = r * 1.05f, center = center)
            drawCircle(c.success.copy(alpha = glow * 0.5f), radius = r * 1.12f, center = center)
        }
        drawCircle(
            Brush.radialGradient(listOf(lerp(c.cardColor, Color.White, 0.04f), lerp(c.cardColor, c.onSurface, 0.16f)), center = center, radius = r),
            radius = r, center = center,
        )

        // double outer ring with fine degree ticks
        drawCircle(c.outline, radius = r, center = center, style = Stroke(2.dp.toPx()))
        drawCircle(c.outline.copy(alpha = 0.6f), radius = r * 0.90f, center = center, style = Stroke(1.dp.toPx()))
        for (deg in 0 until 360 step 5) {
            val major = deg % 30 == 0
            val outer = polar(deg + roseRot, r * 0.90f)
            val inner = polar(deg + roseRot, r * 0.90f - (if (major) 9 else 4).dp.toPx())
            drawLine(c.onSurfaceVariant.copy(alpha = if (major) 0.9f else 0.35f), inner, outer, strokeWidth = (if (major) 2f else 1f).dp.toPx())
        }

        // 8-point compass star (rose)
        // intercardinals first (shorter, behind), then cardinals (longer, front)
        for (deg in intArrayOf(45, 135, 225, 315)) {
            starPoint(center, ::polar, deg + roseRot, r * 0.46f, 9.dp.toPx(), lerp(c.onSurfaceVariant, c.cardColor, 0.55f), c.onSurfaceVariant)
        }
        for (deg in intArrayOf(0, 90, 180, 270)) {
            val isNorth = deg == 0
            val dark = if (isNorth) c.primary else c.onSurface
            starPoint(center, ::polar, deg + roseRot, r * 0.80f, 12.dp.toPx(), lerp(dark, c.cardColor, 0.5f), dark)
        }

        // cardinal letters, upright, just inside the ring
        val cardinals = mapOf(0 to "N", 90 to "E", 180 to "S", 270 to "W")
        for ((deg, t) in cardinals) {
            label(tm, polar(deg + roseRot, r * 0.96f), t, if (deg == 0) c.primary else c.onSurfaceVariant, 13f, FontWeight.Bold)
        }

        // Qibla pointer overlay — distinct ornate arrow + Kaaba (not part of the rose)
        val tip = polar(needleRot, r * 0.66f)
        drawLine(mark.copy(alpha = 0.55f), center, tip, strokeWidth = 2.dp.toPx())
        val perp = needleRot + 90f
        drawPath(
            Path().apply {
                moveTo(tip.x, tip.y)
                val b1 = polar(perp, 5.dp.toPx()); val b2 = polar(perp + 180f, 5.dp.toPx())
                lineTo(b1.x, b1.y); lineTo(b2.x, b2.y); close()
            },
            mark,
        )
        if (aligned) drawCircle(c.success.copy(alpha = glow), radius = 20.dp.toPx(), center = tip)
        kaaba(tip, 13.dp.toPx(), if (aligned) c.success else c.onSurface, c.primary)

        // ornate center hub
        drawCircle(c.cardColor, radius = 12.dp.toPx(), center = center)
        drawCircle(c.outline, radius = 12.dp.toPx(), center = center, style = Stroke(1.5.dp.toPx()))
        drawCircle(mark, radius = 4.dp.toPx(), center = center)

        // fixed top index
        val ix = center.y - r - 2.dp.toPx()
        drawPath(
            Path().apply {
                moveTo(center.x, ix + 12.dp.toPx()); lineTo(center.x - 7.dp.toPx(), ix); lineTo(center.x + 7.dp.toPx(), ix); close()
            },
            c.primary,
        )
    }
}

/** A two-tone compass-rose point (kite): one half [light], the other [dark]. */
private fun DrawScope.starPoint(
    center: Offset,
    polar: (Float, Float) -> Offset,
    deg: Float,
    len: Float,
    half: Float,
    light: Color,
    dark: Color,
) {
    val tip = polar(deg, len)
    val left = polar(deg + 90f, half)
    val right = polar(deg - 90f, half)
    drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(left.x, left.y); lineTo(center.x, center.y); close() }, light)
    drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(right.x, right.y); lineTo(center.x, center.y); close() }, dark)
}

private fun DrawScope.label(tm: TextMeasurer, at: Offset, text: String, color: Color, sizeSp: Float, weight: FontWeight) {
    val layout = tm.measure(text, TextStyle(color = color, fontSize = sizeSp.sp, fontWeight = weight))
    drawText(layout, topLeft = Offset(at.x - layout.size.width / 2f, at.y - layout.size.height / 2f))
}

private fun DrawScope.kaaba(center: Offset, size: Float, cube: Color, band: Color) {
    val topLeft = Offset(center.x - size / 2f, center.y - size / 2f)
    drawRoundRect(cube, topLeft = topLeft, size = Size(size, size), cornerRadius = CornerRadius(3f, 3f))
    drawRect(band, topLeft = Offset(topLeft.x, topLeft.y + size * 0.28f), size = Size(size, size * 0.15f))
}
