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
import androidx.compose.ui.graphics.drawscope.rotate
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
 * Classic (Islamic ornate, kept basic) Qibla dial — brass ring with Arabic-Indic
 * numerals, restrained diamond border, North star accent, Kaaba pointer. Theme-driven.
 */
@Composable
fun QiblaDial(
    headingDeg: Float,
    qiblaDeg: Float,
    aligned: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val tm = rememberTextMeasurer()
    val brass = c.primary // theme stands in for the brass accent

    val roseRot by animateFloatAsState(-headingDeg, tween(170), label = "rose")
    val needleRot by animateFloatAsState(qiblaDeg - headingDeg, tween(170), label = "needle")
    val pulse = rememberInfiniteTransition(label = "pulse")
    val glow by pulse.animateFloat(0.16f, 0.40f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "glow")

    val mark = if (aligned) c.success else brass

    Canvas(modifier.fillMaxWidth().aspectRatio(1f)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f * 0.88f
        fun polar(deg: Float, radius: Float): Offset {
            val a = (deg - 90f) * (PI / 180f).toFloat()
            return Offset(center.x + cos(a) * radius, center.y + sin(a) * radius)
        }

        // shadow, glow, face
        drawCircle(c.onSurface.copy(alpha = 0.10f), radius = r, center = center + Offset(0f, 5.dp.toPx()))
        if (aligned) {
            drawCircle(c.success.copy(alpha = glow), radius = r * 1.05f, center = center)
            drawCircle(c.success.copy(alpha = glow * 0.5f), radius = r * 1.12f, center = center)
        }
        drawCircle(
            Brush.radialGradient(listOf(lerp(c.cardColor, Color.White, 0.04f), lerp(c.cardColor, c.onSurface, 0.14f)), center = center, radius = r),
            radius = r, center = center,
        )

        // brass double ring
        drawCircle(brass, radius = r, center = center, style = Stroke(2.5.dp.toPx()))
        drawCircle(brass.copy(alpha = 0.45f), radius = r * 0.86f, center = center, style = Stroke(1.dp.toPx()))

        // fine minor ticks
        for (deg in 0 until 360 step 10) {
            if (deg % 30 == 0) continue
            val o = polar(deg + roseRot, r * 0.86f)
            val i = polar(deg + roseRot, r * 0.86f - 5.dp.toPx())
            drawLine(c.onSurfaceVariant.copy(alpha = 0.35f), i, o, strokeWidth = 1.dp.toPx())
        }

        // Arabic-Indic numerals + diamond markers at every 30°, upright
        for (deg in 0 until 360 step 30) {
            val diamondAt = polar(deg + roseRot, r * 0.86f)
            diamond(diamondAt, if (deg == 0) 5.dp.toPx() else 3.dp.toPx(), if (deg == 0) brass else c.onSurfaceVariant)
            val text = if (deg == 0) "N" else toArabicDigits(deg)
            label(tm, polar(deg + roseRot, r * 0.71f), text, if (deg == 0) brass else c.onSurface, if (deg == 0) 15f else 11f, if (deg == 0) FontWeight.Bold else FontWeight.Medium)
        }

        // North star accent above the N
        star(polar(0f + roseRot, r * 0.55f), 7.dp.toPx(), brass)

        // Qibla needle (two-tone) + Kaaba
        val perp = needleRot + 90f
        val b1 = polar(perp, 6.dp.toPx()); val b2 = polar(perp + 180f, 6.dp.toPx())
        val tip = polar(needleRot, r * 0.62f)
        val tail = polar(needleRot + 180f, r * 0.30f)
        drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(b1.x, b1.y); lineTo(b2.x, b2.y); close() }, mark)
        drawPath(Path().apply { moveTo(tail.x, tail.y); lineTo(b1.x, b1.y); lineTo(b2.x, b2.y); close() }, c.neutral)
        if (aligned) drawCircle(c.success.copy(alpha = glow), radius = 20.dp.toPx(), center = tip)
        kaaba(tip, 13.dp.toPx(), if (aligned) c.success else c.onSurface, brass)

        // center hub
        drawCircle(c.cardColor, radius = 9.dp.toPx(), center = center)
        drawCircle(brass, radius = 9.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))

        // fixed top index
        val ix = center.y - r - 2.dp.toPx()
        drawPath(Path().apply { moveTo(center.x, ix + 12.dp.toPx()); lineTo(center.x - 7.dp.toPx(), ix); lineTo(center.x + 7.dp.toPx(), ix); close() }, brass)
    }
}

/** Western digits → Arabic-Indic (٠١٢…). */
private fun toArabicDigits(n: Int): String = n.toString().map { ('٠' + (it - '0')) }.joinToString("")

private fun DrawScope.diamond(center: Offset, half: Float, color: Color) {
    rotate(45f, center) {
        drawRect(color, topLeft = Offset(center.x - half, center.y - half), size = Size(half * 2, half * 2))
    }
}

private fun DrawScope.star(center: Offset, radius: Float, color: Color) {
    // simple 4-point star = two crossed thin diamonds
    rotate(0f, center) { drawPath(starPath(center, radius), color) }
    rotate(45f, center) { drawPath(starPath(center, radius * 0.6f), color.copy(alpha = 0.7f)) }
}

private fun starPath(c: Offset, r: Float) = Path().apply {
    moveTo(c.x, c.y - r); lineTo(c.x + r * 0.28f, c.y); lineTo(c.x, c.y + r)
    lineTo(c.x - r * 0.28f, c.y); close()
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
