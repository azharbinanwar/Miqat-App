package com.example.miqatapp.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.PulseDot
import com.example.miqatapp.core.datetime.Now
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.MiqatTimeStatus
import com.example.miqatapp.core.enums.PrayerTrackerStatus
import com.example.miqatapp.core.enums.color
import com.example.miqatapp.core.store.PrayerTrackingStore
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.clear
import com.example.miqatapp.resources.mark_prayer
import com.example.miqatapp.resources.today
import org.jetbrains.compose.resources.stringResource

/** Today's prayer list with per-prayer tracking. Reads the stores; owns its tracking sheet. */
@Composable
fun TodayPrayers() {
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val clock by Now.now.collectAsState()
    val now = clock.time
    val today by MiqatTimesStore.today.collectAsState()
    val tracked by PrayerTrackingStore.tracked.collectAsState()

    val dailyTimes = remember(today) { today.filter { it.miqat in Miqat.DAILY && it.miqat != Miqat.Sunrise } }
    val prayerTimes = dailyTimes.filter { it.miqat.isPrayer }
    // current fard: every prayer runs to the next except Fajr, which ends at sunrise (sunrise→Dhuhr is a gap).
    val sunriseTime = today.firstOrNull { it.miqat == Miqat.Sunrise }?.at?.time
    val startedPrayer = prayerTimes.lastOrNull { it.at.time <= now } ?: prayerTimes.lastOrNull()
    val currentPrayer = when {
        startedPrayer == null -> null
        startedPrayer.miqat == Miqat.Fajr && sunriseTime != null && now >= sunriseTime -> null
        else -> startedPrayer.miqat
    }
    val nextMt = prayerTimes.firstOrNull { it.at.time > now } ?: prayerTimes.firstOrNull()

    var sheetPrayer by remember { mutableStateOf<Miqat?>(null) }

    AppTileGroup(
        title = stringResource(Res.string.today),
        items = dailyTimes.map { mt ->
            val status = when (mt.miqat) {
                currentPrayer -> MiqatTimeStatus.Current
                nextMt?.miqat -> MiqatTimeStatus.Soon
                else -> null
            }
            AppTileItem(
                title = mt.miqat.label(clock.date),
                subtitle = mt.at.time.format(timeFormat.pattern),
                selected = status == MiqatTimeStatus.Current,
                leadingIcon = mt.miqat.icon,
                leadingColor = AppTheme.colors.primary,
                badge = if (status == MiqatTimeStatus.Current) {
                    { PulseDot(color = AppTheme.colors.primary) }
                } else null,
                trailing = if (mt.miqat.isPrayer) {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (status == MiqatTimeStatus.Soon) {
                                Text(MiqatTimeStatus.Soon.label, color = MiqatTimeStatus.Soon.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            TrackControl(tracked[mt.miqat])
                        }
                    }
                } else null,
                onClick = if (mt.miqat.isPrayer) { { sheetPrayer = mt.miqat } } else null,
            )
        },
    )

    sheetPrayer?.let { p ->
        TrackingSheet(
            prayer = p,
            current = tracked[p],
            onSelect = { PrayerTrackingStore.setStatus(p, it); sheetPrayer = null },
            onDismiss = { sheetPrayer = null },
        )
    }
}

@Composable
private fun TrackControl(status: PrayerTrackerStatus?) {
    if (status != null) {
        val sc = status.color
        Box(Modifier.size(32.dp).clip(CircleShape).background(sc.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
            Icon(status.icon, status.label, tint = sc, modifier = Modifier.size(18.dp))
        }
    } else {
        Box(Modifier.size(32.dp).clip(CircleShape).border(1.dp, AppTheme.colors.outlineVariant, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Lucide.Plus, "Track", tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TrackingSheet(
    prayer: Miqat,
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
