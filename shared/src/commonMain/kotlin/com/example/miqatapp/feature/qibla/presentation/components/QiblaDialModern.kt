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
 * Modern Qibla dial — flat, minimal, fully theme-driven. Cleaner than [QiblaDial]:
 * subtle ticks, large cardinals, a slim arrow + Kaaba marker, fixed top index, glow on align.
 */
@Composable
fun QiblaDialModern(
    headingDeg: Float,
    qiblaDeg: Float,
    aligned: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val tm = rememberTextMeasurer()

    val roseRot by animateFloatAsState(-headingDeg, tween(160), label = "rose")
    val needleRot by animateFloatAsState(qiblaDeg - headingDeg, tween(160), label = "needle")

    val pulse = rememberInfiniteTransition(label = "pulse")
    val glow by pulse.animateFloat(0.15f, 0.40f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "glow")

    val faceCenter = c.cardColor
    val faceEdge = lerp(c.cardColor, c.onSurface, 0.06f)
    val mark = if (aligned) c.success else c.primary

    Canvas(modifier.fillMaxWidth().aspectRatio(1f)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f * 0.88f

        fun polar(deg: Float, radius: Float): Offset {
            val a = (deg - 90f) * (PI / 180f).toFloat()
            return Offset(center.x + cos(a) * radius, center.y + sin(a) * radius)
        }

        // soft theme shadow
        drawCircle(c.onSurface.copy(alpha = 0.08f), radius = r, center = center + Offset(0f, 5.dp.toPx()))

        // glow when aligned
        if (aligned) {
            drawCircle(c.success.copy(alpha = glow), radius = r * 1.05f, center = center)
            drawCircle(c.success.copy(alpha = glow * 0.45f), radius = r * 1.12f, center = center)
        }

        // flat face + single hairline ring
        drawCircle(Brush.radialGradient(listOf(faceCenter, faceEdge), center = center, radius = r), radius = r, center = center)
        drawCircle(c.outline.copy(alpha = 0.5f), radius = r, center = center, style = Stroke(1.5.dp.toPx()))

        // minimal ticks: dots at 30°, faint
        for (deg in 0 until 360 step 15) {
            val major = deg % 90 == 0
            val p = polar(deg + roseRot, r - 10.dp.toPx())
            drawCircle(
                c.onSurfaceVariant.copy(alpha = if (major) 0.9f else 0.3f),
                radius = (if (major) 2.5f else 1.5f).dp.toPx(),
                center = p,
            )
        }

        // large cardinals only (modern, uncluttered), upright
        val cardinals = mapOf(0 to "N", 90 to "E", 180 to "S", 270 to "W")
        for ((deg, t) in cardinals) {
            label(tm, polar(deg + roseRot, r * 0.74f), t, if (deg == 0) c.primary else c.onSurfaceVariant, 17f, FontWeight.Bold)
        }

        // slim arrow toward the Qibla
        val perp = needleRot + 90f
        val base1 = polar(perp, 6.dp.toPx())
        val base2 = polar(perp + 180f, 6.dp.toPx())
        val tip = polar(needleRot, r * 0.60f)
        val tail = polar(needleRot + 180f, r * 0.30f)
        drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(base1.x, base1.y); lineTo(base2.x, base2.y); close() }, mark)
        drawLine(c.neutral, center, tail, strokeWidth = 3.dp.toPx())

        // Kaaba marker at the tip
        if (aligned) drawCircle(c.success.copy(alpha = glow), radius = 20.dp.toPx(), center = tip)
        kaaba(tip, 13.dp.toPx(), if (aligned) c.success else c.onSurface, c.primary)

        // center hub
        drawCircle(c.cardColor, radius = 8.dp.toPx(), center = center)
        drawCircle(mark, radius = 8.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))

        // fixed top index (shows the way you face)
        val ix = center.y - r - 2.dp.toPx()
        drawPath(
            Path().apply {
                moveTo(center.x, ix + 12.dp.toPx())
                lineTo(center.x - 7.dp.toPx(), ix)
                lineTo(center.x + 7.dp.toPx(), ix)
                close()
            },
            c.primary,
        )
    }
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
