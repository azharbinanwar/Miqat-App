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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.enums.ReminderPrayer
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

/** Per-prayer auto-silence config (mock, UI-first). */
private class FocusState(startAfter: Int, duration: Int) {
    var enabled by mutableStateOf(false)
    var startAfter by mutableStateOf(startAfter) // minutes after the prayer time before going silent
    var duration by mutableStateOf(duration)     // minutes to stay silent, then restore
}

/**
 * Prayer Focus — a Settings module (Android only). Auto-silences the phone around each prayer, then
 * restores it. Driven by [ReminderPrayer] (icon + label + per-prayer max), so Jumu'ah is included with
 * its longer range. ponytail: mock; real DND control + scheduling wires up later.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerFocusScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    val states = remember {
        ReminderPrayer.entries.associateWith {
            val startAfter = if (it == ReminderPrayer.Fajr || it == ReminderPrayer.Maghrib) 0 else 5
            FocusState(startAfter, duration = if (it.fridayOnly) 60 else 20)
        }
    }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.prayer_focus), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
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
            ReminderPrayer.entries.forEach { rp ->
                val s = states.getValue(rp)
                AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    items = buildList {
                        add(AppTileItem(title = stringResource(rp.labelRes), leadingIcon = rp.icon, trailing = { Switch(checked = s.enabled, onCheckedChange = { s.enabled = it }) }))
                        if (s.enabled) {
                            add(AppTileItem(title = stringResource(Res.string.start_after), trailing = { MiniStepper(s.startAfter, min, { s.startAfter = it }, min = 0, max = 30) }))
                            add(AppTileItem(title = stringResource(Res.string.silence_for), trailing = { MiniStepper(s.duration, min, { s.duration = it }, min = 5, max = rp.maxFocusMin) }))
                        }
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
