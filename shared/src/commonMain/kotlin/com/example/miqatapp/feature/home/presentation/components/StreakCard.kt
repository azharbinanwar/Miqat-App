package com.example.miqatapp.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppCard
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.PrayerTrackerStatus
import com.example.miqatapp.core.store.PrayerTrackingStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.day_streak
import com.example.miqatapp.resources.this_week
import com.example.miqatapp.resources.week_days
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/** Today's tracked count + streak. Streak/best/on-time are placeholders until history lands (Room). */
@Composable
fun StreakCard() {
    val tracked by PrayerTrackingStore.tracked.collectAsState()
    val total = Miqat.PRAYERS.size
    val done = tracked.count { it.value != PrayerTrackerStatus.Missed }
    val streak = 12; val best = 21; val onTimePct = 85 // ponytail: demo values until history exists

    AppCard(padding = 18.dp, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { done / total.toFloat() },
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 7.dp,
                    color = AppTheme.colors.surfaceTint,
                    trackColor = AppTheme.colors.neutralMutedContainer,
                )
                Text("$done/$total", color = AppTheme.colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 17.sp)
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

/** Day status dot: full = check, partial = dash, none = empty outline. */
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
