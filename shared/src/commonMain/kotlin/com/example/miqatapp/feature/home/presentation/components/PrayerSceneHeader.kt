package com.example.miqatapp.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.example.miqatapp.core.components.SehriInfoSheet
import com.example.miqatapp.core.datetime.HijriMonth
import com.example.miqatapp.core.datetime.Now
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.datetime.labelRes
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.home.presentation.MosqueScene
import com.example.miqatapp.feature.home.presentation.liveSkyState
import com.example.miqatapp.feature.miqat.domain.MiqatTime
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.hijri_era
import com.example.miqatapp.resources.iftar
import com.example.miqatapp.resources.sehri
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

/** Collapsing prayer header with the live scene. Reads the stores itself; caller only passes [fraction]. */
@Composable
fun PrayerSceneHeader(
    fraction: Float,
    modifier: Modifier = Modifier,
    expandedHeight: Dp = 380.dp,
    collapsedHeight: Dp = 116.dp,
    onMenuClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
) {
    val place by LocationStore.activePlace.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val hijri by SettingsStore.hijriDate.collectAsState()
    val sehriRef by SettingsStore.sehriReference.collectAsState()
    val clock by Now.now.collectAsState()
    val now = clock.time
    val today by MiqatTimesStore.today.collectAsState()

    val prayerTimes = today.filter { it.miqat.isPrayer }
    val nextMt = prayerTimes.firstOrNull { it.at.time > now } ?: prayerTimes.firstOrNull()
    val prayer = nextMt?.miqat ?: Miqat.Fajr
    val nextTime = nextMt?.at?.time?.format(timeFormat.pattern) ?: ""
    val countdown = nextMt?.let {
        val secs = ((it.at.time.toSecondOfDay() - now.toSecondOfDay()) + 24 * 3600) % (24 * 3600)
        val h = secs / 3600; val m = (secs % 3600) / 60; val s = secs % 60
        if (h > 0) "in ${h}h ${m}m ${s}s" else "in ${m}m ${s}s"
    } ?: ""
    val sky = remember(now, today) { liveSkyState(now, today) }
    val period = remember(now, today) { currentPeriod(now, today) }
    val dateLabel = "${stringResource(clock.date.dayOfWeek.labelRes)}, ${hijri.day} ${HijriMonth.of(hijri.month).label()} ${hijri.year} ${stringResource(Res.string.hijri_era)}"

    val ramadan = hijri.month == 9
    val sehri = if (ramadan) today.firstOrNull { it.miqat == sehriRef }?.at?.time?.format(timeFormat.pattern) else null
    val iftar = if (ramadan) today.firstOrNull { it.miqat == Miqat.Maghrib }?.at?.time?.format(timeFormat.pattern) else null
    val sunrise = today.firstOrNull { it.miqat == Miqat.Sunrise }?.at?.time?.format(timeFormat.pattern)
    val sunset = today.firstOrNull { it.miqat == Miqat.Sunset }?.at?.time?.format(timeFormat.pattern)
    var showSehriInfo by remember { mutableStateOf(false) }

    val headerHeight = lerp(expandedHeight, collapsedHeight, fraction)
    val headerCorner = lerp(28.dp, 0.dp, fraction)
    val expandedAlpha = (1f - fraction * 1.7f).coerceIn(0f, 1f)
    val slimAlpha = ((fraction - 0.35f) / 0.65f).coerceIn(0f, 1f)

    Box(
        modifier.fillMaxWidth().height(headerHeight)
            .clip(RoundedCornerShape(bottomStart = headerCorner, bottomEnd = headerCorner)),
    ) {
        MosqueScene(sky, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))))

        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMenuClick) { Icon(Lucide.Menu, "Menu", tint = Color.White) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(1f - slimAlpha)) {
                    Icon(Lucide.MapPin, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(place.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(slimAlpha)) {
                    Icon(prayer.icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${prayer.name} · $nextTime", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            IconButton(onClick = onNotificationsClick) { Icon(Lucide.Bell, "Notifications", tint = Color.White) }
        }

        if (expandedAlpha > 0f) {
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp).alpha(expandedAlpha)) {
                Text("NOW", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.6.sp)
                Spacer(Modifier.height(3.dp))
                Text(stringResource(period.labelRes), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("${prayer.name}   $nextTime · $countdown", color = Color.White.copy(alpha = 0.92f), fontSize = 13.sp)
                if (sehri != null || iftar != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (sehri != null) RamadanChip(Lucide.Moon, stringResource(Res.string.sehri), sehri)
                        if (sehri != null && iftar != null) Spacer(Modifier.width(8.dp))
                        if (iftar != null) RamadanChip(Lucide.Sunset, stringResource(Res.string.iftar), iftar)
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Lucide.Info, "About Sehri", tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp).clip(CircleShape).clickable { showSehriInfo = true },
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dateLabel, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                    )
                    if (sunrise != null || sunset != null) {
                        Spacer(Modifier.width(12.dp))
                        if (sunrise != null) SunTime(Lucide.Sunrise, sunrise)
                        if (sunrise != null && sunset != null) Spacer(Modifier.width(10.dp))
                        if (sunset != null) SunTime(Lucide.Sunset, sunset)
                    }
                }
            }
        }
    }

    if (showSehriInfo) SehriInfoSheet(onDismiss = { showSehriInfo = false })
}

/** The daily marker we're in now. Wraps overnight to Isha, so it's never blank. */
private fun currentPeriod(now: LocalTime, times: List<MiqatTime>): Miqat {
    val pts = Miqat.PERIODS
        .mapNotNull { m -> times.firstOrNull { it.miqat == m }?.let { m to (it.at.time.hour * 60 + it.at.time.minute) } }
        .sortedBy { it.second }
    if (pts.isEmpty()) return Miqat.Isha
    val n = now.hour * 60 + now.minute
    val idx = pts.indexOfLast { it.second <= n }
    return if (idx >= 0) pts[idx].first else pts.last().first
}

/** Muted sunrise / sunset time over the scene. */
@Composable
private fun SunTime(icon: ImageVector, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(time, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

/** Small translucent pill for a Ramadan time (Sehri / Iftar). */
@Composable
private fun RamadanChip(icon: ImageVector, label: String, time: String) {
    Row(
        Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.16f)).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text("$label  $time", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
