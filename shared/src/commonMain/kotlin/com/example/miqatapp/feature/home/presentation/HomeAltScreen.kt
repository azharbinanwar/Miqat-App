package com.example.miqatapp.feature.home.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.TrendingUp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.PrayerTimeStatus
import com.example.miqatapp.core.enums.color
import com.example.miqatapp.core.enums.onColor
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.navigation.LocalNavController
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class AltPrayer(val prayer: Miqat, val time: String, val status: PrayerTimeStatus?)

private val altTimes = listOf(
    AltPrayer(Miqat.Fajr, "4:32 AM", null),
    AltPrayer(Miqat.Sunrise, "5:58 AM", null),
    AltPrayer(Miqat.Dhuhr, "12:21 PM", null),
    AltPrayer(Miqat.Asr, "3:47 PM", PrayerTimeStatus.Current),
    AltPrayer(Miqat.Maghrib, "6:44 PM", PrayerTimeStatus.Soon),
    AltPrayer(Miqat.Isha, "8:14 PM", null),
)

@Composable
fun HomeAltScreen() {
    val nav = LocalNavController.current
    Column(
        Modifier.fillMaxSize().background(AppTheme.colors.scaffoldBackgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState()),
    ) {
        // top bar
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), "Back", tint = AppTheme.colors.onSurface) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.MapPin, null, tint = AppTheme.colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Makkah", color = AppTheme.colors.onSurface, fontWeight = FontWeight.SemiBold)
                }
            }
            IconButton(onClick = { /* TODO */ }) { Icon(Lucide.Bell, "Notifications", tint = AppTheme.colors.onSurface) }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Friday, 12 Dhul-Hijjah 1447",
            color = AppTheme.colors.onSurfaceVariant, fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(16.dp))
        RadialDial(next = Miqat.Maghrib, time = "6:44 PM", countdown = "02:14:30", progress = 0.62f, times = altTimes)

        Spacer(Modifier.height(24.dp))
        Text(
            "Today", color = AppTheme.colors.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            altTimes.forEach { PrayerChipCard(it) }
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Lucide.Flame, "12", "Streak", AppTheme.colors.warning, Modifier.weight(1f))
            StatTile(Lucide.Check, "2/5", "Today", AppTheme.colors.prayedColor, Modifier.weight(1f))
            StatTile(Lucide.TrendingUp, "85%", "This week", AppTheme.colors.primary, Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RadialDial(next: Miqat, time: String, countdown: String, progress: Float, times: List<AltPrayer>) {
    val ring = next.color
    val track = AppTheme.colors.neutralMutedContainer
    val cardBg = AppTheme.colors.cardColor
    val dotColors = times.map { it.prayer.color }
    val sun = Color(0xFFFFE082)

    Box(Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(260.dp)) {
            val stroke = 16.dp.toPx()
            val r = size.minDimension / 2 - stroke / 2 - 8.dp.toPx()
            val c = Offset(size.width / 2, size.height / 2)

            drawCircle(track, radius = r, center = c, style = Stroke(width = stroke))
            drawArc(
                color = ring,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            val n = dotColors.size
            dotColors.forEachIndexed { i, col ->
                val ang = (-90f + i / (n - 1f) * 360f) * (PI / 180f).toFloat()
                val p = Offset(c.x + r * cos(ang), c.y + r * sin(ang))
                drawCircle(cardBg, radius = 7.dp.toPx(), center = p)
                drawCircle(col, radius = 5.dp.toPx(), center = p)
            }

            val mAng = (-90f + progress * 360f) * (PI / 180f).toFloat()
            val mp = Offset(c.x + r * cos(mAng), c.y + r * sin(mAng))
            drawCircle(sun.copy(alpha = 0.30f), radius = 15.dp.toPx(), center = mp)
            drawCircle(sun, radius = 8.dp.toPx(), center = mp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(ring.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(next.icon, null, tint = ring, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Next · ${next.name}", color = AppTheme.colors.onSurfaceVariant, fontSize = 13.sp)
            Text(countdown, color = AppTheme.colors.onSurface, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text(time, color = AppTheme.colors.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PrayerChipCard(item: AltPrayer) {
    val c = item.prayer.color
    val on = item.prayer.onColor
    Column(
        Modifier.width(96.dp).clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(c, c.copy(alpha = 0.75f))))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(on.copy(alpha = 0.20f)), contentAlignment = Alignment.Center) {
            Icon(item.prayer.icon, null, tint = on, modifier = Modifier.size(22.dp))
        }
        Text(item.prayer.name, color = on, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(item.time.removeSuffix(" AM").removeSuffix(" PM"), color = on.copy(alpha = 0.85f), fontSize = 12.sp)
    }
}

@Composable
private fun StatTile(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(AppTheme.colors.cardColor).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
        Text(value, color = AppTheme.colors.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AppTheme.colors.onSurfaceVariant, fontSize = 12.sp)
    }
}
