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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.navigation.LocalNavController
import kotlin.math.PI
import kotlin.math.sin

private data class Scene(val arcPos: Float, val sky: List<Color>, val moon: Boolean, val night: Float)

private fun sceneFor(p: Miqat): Scene = when (p) {
    Miqat.Fajr -> Scene(0.10f, listOf(Color(0xFF1A1740), Color(0xFF3B2E63), Color(0xFF7A5A86)), moon = true, night = 0.70f)
    Miqat.Sunrise -> Scene(0.15f, listOf(Color(0xFF2A4A8A), Color(0xFFB85C73), Color(0xFFF0A85C)), moon = false, night = 0.18f)
    Miqat.Dhuhr -> Scene(0.50f, listOf(Color(0xFF2E83C9), Color(0xFF6FB6E8), Color(0xFFC6E6FB)), moon = false, night = 0f)
    Miqat.Asr -> Scene(0.80f, listOf(Color(0xFF3E78B0), Color(0xFF8FB4D8), Color(0xFFE6D6A8)), moon = false, night = 0.08f)
    Miqat.Maghrib -> Scene(0.94f, listOf(Color(0xFF2B1E55), Color(0xFF8E3A63), Color(0xFFE8843C)), moon = false, night = 0.42f)
    Miqat.Isha -> Scene(0.55f, listOf(Color(0xFF050912), Color(0xFF0E1430), Color(0xFF221A45)), moon = true, night = 0.95f)
    // non-carousel points reuse the nearest daily scene (carousel only cycles Miqat.DAILY)
    Miqat.Imsak -> sceneFor(Miqat.Fajr)
    Miqat.Duha, Miqat.Zawal -> sceneFor(Miqat.Dhuhr)
    Miqat.Sunset -> sceneFor(Miqat.Maghrib)
    Miqat.Midnight, Miqat.LastThird -> sceneFor(Miqat.Isha)
}

/**
 * Reusable mosque + mountains scene for [prayer]. Sky color, sun/moon position and
 * silhouette darkness animate when [prayer] changes. Use full-screen or as a header.
 */
private val starField = listOf(
    0.10f to 0.22f, 0.20f to 0.46f, 0.34f to 0.16f, 0.48f to 0.36f, 0.60f to 0.24f,
    0.72f to 0.42f, 0.84f to 0.18f, 0.92f to 0.52f, 0.16f to 0.62f, 0.44f to 0.60f,
    0.68f to 0.56f, 0.88f to 0.66f,
)

@Composable
fun MosqueScene(prayer: Miqat, modifier: Modifier = Modifier) {
    val scene = sceneFor(prayer)
    val density = LocalDensity.current
    val topInsetPx = WindowInsets.statusBars.getTop(density).toFloat()
    val pos by animateFloatAsState(scene.arcPos, tween(700), label = "pos")
    val night by animateFloatAsState(scene.night, tween(700), label = "night")
    val c0 by animateColorAsState(scene.sky[0], tween(700), label = "c0")
    val c1 by animateColorAsState(scene.sky[1], tween(700), label = "c1")
    val c2 by animateColorAsState(scene.sky[2], tween(700), label = "c2")
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        0.20f, 0.50f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "glow",
    )

    val orbColor = if (scene.moon) Color(0xFFE8EAF6) else Color(0xFFFFD54F)
    val baseMtn = lerp(Color(0xFF1A2A3A), Color(0xFF070B16), night)

    Box(modifier.background(Brush.verticalGradient(listOf(c0, c1, c2)))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val horizonY = h * 0.66f
            val topLimit = topInsetPx + 64.dp.toPx()   // clear the notch incl. the glow halo
            val amp = (horizonY - topLimit).coerceAtLeast(1f)  // safe when collapsed small

            // stars (night only)
            if (night > 0.05f) {
                starField.forEach { (xf, yf) ->
                    drawCircle(Color.White.copy(alpha = night * 0.7f), 1.6.dp.toPx(), Offset(xf * w, yf * horizonY))
                }
            }

            // sun / moon on the arc (peak clamped below the notch)
            val pad = w * 0.12f
            val ox = pad + pos * (w - 2 * pad)
            val oy = horizonY - amp * sin(pos * PI).toFloat()
            val center = Offset(ox, oy)
            if (scene.moon) {
                // crescent: carve with the sky color at the orb's height (no glow → no eclipse look)
                val frac = (oy / h).coerceIn(0f, 1f)
                val skyHere = if (frac < 0.5f) lerp(c0, c1, frac * 2f) else lerp(c1, c2, (frac - 0.5f) * 2f)
                drawCircle(orbColor, 22.dp.toPx(), center)
                drawCircle(skyHere, 19.dp.toPx(), Offset(ox + 9.dp.toPx(), oy - 6.dp.toPx()))
            } else {
                drawCircle(orbColor.copy(alpha = glow * 0.5f), 56.dp.toPx(), center)
                drawCircle(orbColor.copy(alpha = glow), 36.dp.toPx(), center)
                drawCircle(orbColor, 24.dp.toPx(), center)
            }

            // multi-shade layered mountains (back hazy → front dark)
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

/** Full-screen interactive demo — tap to switch prayer. */
@Composable
fun MosqueSceneScreen() {
    val nav = LocalNavController.current
    val prayers = Miqat.DAILY
    var index by remember { mutableStateOf(prayers.indexOf(Miqat.Dhuhr)) }

    Box(Modifier.fillMaxSize().clickable { index = (index + 1) % prayers.size }) {
        MosqueScene(prayers[index], Modifier.fillMaxSize())
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), "Back", tint = Color.White) }
            Text(prayers[index].name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        Text(
            "Tap anywhere to change prayer",
            color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
        )
    }
}
