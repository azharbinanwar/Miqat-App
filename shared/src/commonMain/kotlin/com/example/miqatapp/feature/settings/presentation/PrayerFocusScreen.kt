package com.example.miqatapp.feature.settings.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.constants.defaults.FocusDefaults
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.store.PrayerFocusStore
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import kotlinx.datetime.LocalTime
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.silence_phone_around_each_prayer_set_separately
import com.example.miqatapp.resources.silence_for
import com.example.miqatapp.resources.start_after
import com.example.miqatapp.resources.prayer_focus
import com.example.miqatapp.resources.minutes_short
import org.jetbrains.compose.resources.stringResource
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.MiniStepper

/** "Silent 4:40 to 5:10 (30 min)" from today's [prayer] time + the offsets. ponytail: strings inline. */
private fun silentWindow(prayer: LocalTime, after: Int, duration: Int, pattern: String): String {
    val start = LocalTime.fromSecondOfDay((prayer.toSecondOfDay() + after * 60) % 86400)
    val end = LocalTime.fromSecondOfDay((prayer.toSecondOfDay() + (after + duration) * 60) % 86400)
    return "Silent ${start.format(pattern)} to ${end.format(pattern)} ($duration min)"
}

/**
 * Prayer Focus (Android only). Auto-silences the phone around each prayer, then restores it. Rows are the
 * five daily prayers plus Jumu'ah; labels/icons come from Miqat, default windows from FocusDefaults.
 * ponytail: mock; real DND control + scheduling wires up later.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerFocusScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val today by MiqatTimesStore.today.collectAsState()
    val configs by PrayerFocusStore.configs.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.prayer_focus)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
            )
        },
    ) { pad ->
        val min = stringResource(Res.string.minutes_short)
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(
                stringResource(Res.string.silence_phone_around_each_prayer_set_separately),
                fontSize = 13.sp, color = c.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(12.dp))
            FocusDefaults.rows.forEach { row ->
                val cfg = configs.getValue(row.key)
                val label = if (row.friday) Miqat.jumuahLabel() else row.miqat.label()
                val prayerTime = today.firstOrNull { it.miqat == row.miqat }?.at?.time
                val title = if (prayerTime != null) "$label · ${prayerTime.format(timeFormat.pattern)}" else label
                AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    items = buildList {
                        add(AppTileItem(
                            title = title,
                            subtitle = if (cfg.enabled && prayerTime != null) silentWindow(prayerTime, cfg.startAfter, cfg.duration, timeFormat.pattern) else null,
                            leadingIcon = row.miqat.icon,
                            trailing = { Switch(checked = cfg.enabled, onCheckedChange = { PrayerFocusStore.setEnabled(row.key, it) }) },
                        ))
                        if (cfg.enabled) {
                            add(AppTileItem(title = stringResource(Res.string.start_after), trailing = { MiniStepper(cfg.startAfter, min, { PrayerFocusStore.setStartAfter(row.key, it) }, min = 0, max = row.default.max) }))
                            add(AppTileItem(title = stringResource(Res.string.silence_for), trailing = { MiniStepper(cfg.duration, min, { PrayerFocusStore.setDuration(row.key, it) }, min = 5, max = row.default.max) }))
                        }
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
