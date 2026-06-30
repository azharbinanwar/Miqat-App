package com.example.miqatapp.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.MoonStar
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.enums.Prayer
import com.example.miqatapp.feature.home.presentation.components.PrayerSceneHeader
import com.example.miqatapp.core.enums.PrayerTimeStatus
import com.example.miqatapp.core.enums.PrayerTrackerStatus
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.clear
import com.example.miqatapp.resources.day_streak
import com.example.miqatapp.resources.mark_prayer
import com.example.miqatapp.resources.recite_before_sleep_after_isha
import com.example.miqatapp.resources.surah_al_mulk
import com.example.miqatapp.resources.this_week
import com.example.miqatapp.resources.today
import com.example.miqatapp.resources.verse_of_the_day
import com.example.miqatapp.resources.week_days
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import com.example.miqatapp.core.enums.color
import com.example.miqatapp.core.widgets.AppBottomSheet
import com.example.miqatapp.core.widgets.AppCard
import com.example.miqatapp.core.widgets.LocalDrawerState
import com.example.miqatapp.core.widgets.AppTile
import com.example.miqatapp.core.widgets.AppTileGroup
import com.example.miqatapp.core.widgets.AppTileItem
import com.example.miqatapp.core.widgets.PulseDot

private data class SceneRow(
    val prayer: Prayer,
    val time: String,
    val status: PrayerTimeStatus?,
    val tracked: PrayerTrackerStatus? = null,
    val trackable: Boolean = true,
)

private val sceneRows = listOf(
    SceneRow(Prayer.Fajr, "4:32 AM", null, PrayerTrackerStatus.PrayedOnTime),
    SceneRow(Prayer.Sunrise, "5:58 AM", null, trackable = false),
    SceneRow(Prayer.Dhuhr, "12:21 PM", null, PrayerTrackerStatus.PrayedWithJamaat),
    SceneRow(Prayer.Asr, "3:47 PM", PrayerTimeStatus.Current),
    SceneRow(Prayer.Maghrib, "6:44 PM", PrayerTimeStatus.Soon),
    SceneRow(Prayer.Isha, "8:14 PM", null),
)

private val ExpandedHeader = 380.dp
private val CollapsedHeader = 116.dp

@Composable
fun HomeScreen() {
    val prayers = Prayer.entries
    var index by remember { mutableStateOf(prayers.indexOf(Prayer.Maghrib)) }
    val next = prayers[index]
    val nextTime = sceneRows.firstOrNull { it.prayer == next }?.time ?: ""

    val tracked = remember { mutableStateMapOf<Prayer, PrayerTrackerStatus?>().apply { sceneRows.forEach { put(it.prayer, it.tracked) } } }
    var sheetPrayer by remember { mutableStateOf<Prayer?>(null) }
    val total = sceneRows.count { it.trackable }
    val prayedCount = sceneRows.count { it.trackable && tracked[it.prayer].let { s -> s != null && s != PrayerTrackerStatus.Missed } }

    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val rangePx = with(density) { (ExpandedHeader - CollapsedHeader).toPx() }
    val fraction = (scroll.value / rangePx).coerceIn(0f, 1f)
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(AppTheme.colors.scaffoldBackgroundColor)) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Spacer(Modifier.height(ExpandedHeader))
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StreakCard(today = prayedCount, total = total, streak = 12, best = 21, onTimePct = 85)
                AppTileGroup(
                    title = stringResource(Res.string.today),
                    items = sceneRows.map { row ->
                        AppTileItem(
                            title = row.prayer.name,
                            subtitle = row.time,
                            selected = row.status == PrayerTimeStatus.Current,
                            leadingIcon = row.prayer.icon,
                            leadingColor = AppTheme.colors.primary,
                            badge = if (row.status == PrayerTimeStatus.Current) {
                                { PulseDot(color = AppTheme.colors.primary) }
                            } else null,
                            trailing = if (row.trackable) {
                                {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (row.status == PrayerTimeStatus.Soon) {
                                            Text(PrayerTimeStatus.Soon.label, color = PrayerTimeStatus.Soon.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        TrackControl(tracked[row.prayer])
                                    }
                                }
                            } else null,
                            onClick = if (row.trackable) { { sheetPrayer = row.prayer } } else null,
                        )
                    },
                )
                MulkReminderCard()
                DailyVerseCard()
                Spacer(Modifier.height(8.dp))
            }
        }

        PrayerSceneHeader(
            prayer = next,
            fraction = fraction,
            locationName = "Makkah",
            dateLabel = "Friday, 12 Dhul-Hijjah 1447",
            nextTime = nextTime,
            countdown = "in 2h 14m",
            expandedHeight = ExpandedHeader,
            collapsedHeight = CollapsedHeader,
            onMenuClick = { scope.launch { drawerState.open() } },
            onTap = { index = (index + 1) % prayers.size },
        )

        sheetPrayer?.let { p ->
            TrackingSheet(
                prayer = p,
                current = tracked[p],
                onSelect = { tracked[p] = it; sheetPrayer = null },
                onDismiss = { sheetPrayer = null },
            )
        }
    }
}

@Composable
private fun TrackControl(tracked: PrayerTrackerStatus?) {
    if (tracked != null) {
        val sc = tracked.color
        Box(Modifier.size(32.dp).clip(CircleShape).background(sc.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
            Icon(tracked.icon, tracked.label, tint = sc, modifier = Modifier.size(18.dp))
        }
    } else {
        Box(Modifier.size(32.dp).clip(CircleShape).border(1.dp, AppTheme.colors.outlineVariant, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Lucide.Plus, "Track", tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TrackingSheet(
    prayer: Prayer,
    current: PrayerTrackerStatus?,
    onSelect: (PrayerTrackerStatus?) -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(onDismiss = onDismiss) {
        Text(
            stringResource(Res.string.mark_prayer, prayer.name), color = AppTheme.colors.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
        )
        AppTileGroup(
            items = PrayerTrackerStatus.entries.map { st ->
                val sc = st.color
                AppTileItem(
                    title = st.label,
                    selected = st == current,
                    leadingIcon = st.icon,
                    leadingColor = sc,
                    trailing = if (st == current) { { Icon(Lucide.Check, null, tint = sc, modifier = Modifier.size(20.dp)) } } else null,
                    onClick = { onSelect(st) },
                )
            },
        )
        if (current != null) {
            AppTile(
                title = stringResource(Res.string.clear),
                leadingIcon = Lucide.X,
                leadingColor = AppTheme.colors.onSurfaceVariant,
                onClick = { onSelect(null) },
            )
        }
    }
}

@Composable
private fun StreakCard(today: Int, total: Int, streak: Int, best: Int, onTimePct: Int) {
    AppCard(padding = 18.dp, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { today / total.toFloat() },
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 7.dp,
                    color = AppTheme.colors.surfaceTint,
                    trackColor = AppTheme.colors.neutralMutedContainer,
                )
                Text("$today/$total", color = AppTheme.colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.Flame, null, tint = AppTheme.colors.warning, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("$streak", color = AppTheme.colors.onSurface, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.day_streak), color = AppTheme.colors.onSurfaceVariant, fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("Best $best days  ·  $onTimePct% on time", color = AppTheme.colors.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.this_week), color = AppTheme.colors.onSurfaceVariant, fontSize = 11.sp)
            val days = stringArrayResource(Res.array.week_days)
            val levels = listOf(2, 2, 1, 2, 2, 0, 2)
            val todayIndex = 4
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEachIndexed { i, d ->
                    val isToday = i == todayIndex
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DayDot(levels[i])
                        Text(
                            d,
                            color = if (isToday) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

/** Day status dot: full = check, partial = dash, none/upcoming = empty outline. */
@Composable
private fun DayDot(level: Int) {
    val c = AppTheme.colors
    when (level) {
        2 -> Box(Modifier.size(34.dp).clip(CircleShape).background(c.success.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
            Icon(Lucide.Check, null, tint = c.success, modifier = Modifier.size(18.dp))
        }
        1 -> Box(Modifier.size(34.dp).clip(CircleShape).background(c.warning.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
            Icon(Lucide.Minus, null, tint = c.warning, modifier = Modifier.size(18.dp))
        }
        else -> Box(Modifier.size(34.dp).clip(CircleShape).border(1.5.dp, c.outlineVariant, CircleShape))
    }
}

@Composable
private fun MulkReminderCard() {
    AppTile(
        title = stringResource(Res.string.surah_al_mulk),
        subtitle = stringResource(Res.string.recite_before_sleep_after_isha),
        leadingIcon = Lucide.MoonStar,
        leadingColor = AppTheme.colors.primary,
        trailing = { Box(Modifier.size(8.dp).clip(CircleShape).background(AppTheme.colors.success)) },
    )
}

@Composable
private fun DailyVerseCard() {
    AppCard(padding = 18.dp, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Lucide.BookOpen, null, tint = AppTheme.colors.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.verse_of_the_day), color = AppTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("\"Indeed, prayer prohibits immorality and wrongdoing.\"", color = AppTheme.colors.onSurface, fontSize = 15.sp)
        Text("— Surah Al-'Ankabut 29:45", color = AppTheme.colors.onSurfaceVariant, fontSize = 12.sp)
    }
}


