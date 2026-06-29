package com.example.miqatapp.feature.home.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.core.enums.Prayer
import com.example.miqatapp.core.navigation.LocalNavController
import kotlin.math.PI
import kotlin.math.sin

private val dawnSky = listOf(Color(0xFF0B1026), Color(0xFF24205E), Color(0xFF6D5BA6))
private val noonSky = listOf(Color(0xFF0C4A6E), Color(0xFF1E88C7), Color(0xFF7DD3FC))
private val duskSky = listOf(Color(0xFF1E1B4B), Color(0xFF7C2D5A), Color(0xFFD97706))
private val nightSky = listOf(Color(0xFF020617), Color(0xFF0B1026), Color(0xFF1E1B4B))

private fun lerp3(a: List<Color>, b: List<Color>, t: Float) = a.indices.map { lerp(a[it], b[it], t) }

/** Animated sun journey across the sky — original take, not a copy of any specific design. */
@Composable
fun PrayerAnimationScreen() {
    val nav = LocalNavController.current
    val transition = rememberInfiniteTransition(label = "sky")
    // 0..1 over the whole day loop
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "phase",
    )
    val glow by transition.animateFloat(
        initialValue = 0.18f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "glow",
    )

    // sky gradient morphs dawn -> noon -> dusk -> night across the loop
    val sky = when {
        phase < 0.33f -> lerp3(dawnSky, noonSky, phase / 0.33f)
        phase < 0.66f -> lerp3(noonSky, duskSky, (phase - 0.33f) / 0.33f)
        else -> lerp3(duskSky, nightSky, (phase - 0.66f) / 0.34f)
    }
    val isNight = phase > 0.5f

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(sky))) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Lucide.ArrowLeft, "Back", tint = Color.White) }
                Text("Sun Journey", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            }

            Spacer(Modifier.height(40.dp))
            SkyArc(phase = phase, glow = glow, isNight = isNight)

            Spacer(Modifier.height(28.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentLabel(phase), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(if (isNight) "Night sky" else "Daytime", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }

            Spacer(Modifier.height(36.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Prayer.entries.forEach { p ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(8.dp).background(Color.White.copy(alpha = 0.9f), androidx.compose.foundation.shape.CircleShape))
                        Text(p.name.take(3), color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

private fun currentLabel(phase: Float): String = when {
    phase < 0.17f -> "Fajr"
    phase < 0.34f -> "Sunrise"
    phase < 0.5f -> "Dhuhr"
    phase < 0.67f -> "Asr"
    phase < 0.83f -> "Maghrib"
    else -> "Isha"
}

@Composable
private fun SkyArc(phase: Float, glow: Float, isNight: Boolean) {
    val orb = if (isNight) Color(0xFFE8EAF6) else Color(0xFFFFE082)
    Canvas(Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 28.dp)) {
        val w = size.width
        val baseY = size.height - 8.dp.toPx()
        val amp = size.height - 40.dp.toPx()
        fun xAt(t: Float) = t * w
        fun yAt(t: Float) = baseY - amp * sin(t * PI).toFloat()

        // horizon line
        drawLine(
            Color.White.copy(alpha = 0.25f),
            start = Offset(0f, baseY), end = Offset(w, baseY), strokeWidth = 1.5.dp.toPx(),
        )

        // full faint arc
        val full = androidx.compose.ui.graphics.Path()
        var t = 0f
        while (t <= 1f) { val x = xAt(t); val y = yAt(t); if (t == 0f) full.moveTo(x, y) else full.lineTo(x, y); t += 0.02f }
        drawPath(full, color = Color.White.copy(alpha = 0.35f), style = Stroke(width = 2.dp.toPx()))

        // traveled arc
        val travelled = androidx.compose.ui.graphics.Path(); travelled.moveTo(xAt(0f), yAt(0f))
        var s = 0f
        while (s <= phase) { travelled.lineTo(xAt(s), yAt(s)); s += 0.02f }
        drawPath(travelled, color = orb, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

        // prayer dots
        for (i in 0..5) {
            val f = i / 5f
            drawCircle(Color.White.copy(alpha = 0.5f), radius = 4.dp.toPx(), center = Offset(xAt(f), yAt(f)))
        }

        // the orb (sun/moon) with pulsing glow + rays via radial gradient
        val ox = xAt(phase); val oy = yAt(phase)
        drawCircle(orb.copy(alpha = glow * 0.5f), radius = 34.dp.toPx(), center = Offset(ox, oy))
        drawCircle(orb.copy(alpha = glow), radius = 20.dp.toPx(), center = Offset(ox, oy))
        drawCircle(orb, radius = 11.dp.toPx(), center = Offset(ox, oy))
    }
}
