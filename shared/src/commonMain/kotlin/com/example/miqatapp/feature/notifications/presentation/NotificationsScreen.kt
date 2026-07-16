package com.example.miqatapp.feature.notifications.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.AppSwitch
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.core.constants.defaults.NotificationDefaults as N
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.TimeFormat
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.notifications.presentation.components.NotifDhikr
import com.example.miqatapp.feature.notifications.store.NotificationStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.adhan_sound
import com.example.miqatapp.resources.after_isha
import com.example.miqatapp.resources.all_alerts
import com.example.miqatapp.resources.at_prayer_time
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.beep
import com.example.miqatapp.resources.friday
import com.example.miqatapp.resources.full_adhan
import com.example.miqatapp.resources.ishraq
import com.example.miqatapp.resources.jamaat_after_start
import com.example.miqatapp.resources.jamaat_reminder
import com.example.miqatapp.resources.last_third_of_the_night
import com.example.miqatapp.resources.master_switch_for_every_reminder
import com.example.miqatapp.resources.mid_morning
import com.example.miqatapp.resources.minutes_short
import com.example.miqatapp.resources.notifications
import com.example.miqatapp.resources.prayer_jumuah
import com.example.miqatapp.resources.remind_before
import com.example.miqatapp.resources.remind_if_missed
import com.example.miqatapp.resources.reminder_time
import com.example.miqatapp.resources.save
import com.example.miqatapp.resources.silent
import com.example.miqatapp.resources.surah_al_kahf
import com.example.miqatapp.resources.surah_al_mulk
import com.example.miqatapp.resources.tahajjud
import com.example.miqatapp.resources.takbir
import com.example.miqatapp.resources.vibration
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

/**
 * Notifications, Focus-style: every alert is its own tile group. Flipping a prayer, Jumu'ah, or surah on
 * grows that same group with its settings. State is read from and written to [NotificationStore].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val s by NotificationStore.settings.collectAsState()

    // UI-only: which prayer's sound sheet is open, and whether the Kahf time picker is open.
    var soundFor by remember { mutableStateOf<String?>(null) }
    var kahfPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.notifications)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
            )
        },
    ) { pad ->
        val min = stringResource(Res.string.minutes_short)
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            AppTileGroup(items = listOf(toggleTile(stringResource(Res.string.all_alerts), s.allAlerts, NotificationStore::setAllAlerts, stringResource(Res.string.master_switch_for_every_reminder))))

            // One group per prayer: toggle, then its settings on enable.
            Miqat.PRAYERS.forEach { p ->
                val cfg = s.prayers.getValue(p.key)
                AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    items = buildList {
                        add(AppTileItem(title = stringResource(p.labelRes), leadingIcon = p.icon, trailing = { AppSwitch(checked = cfg.enabled, onCheckedChange = { NotificationStore.setPrayerEnabled(p.key, it) }) }))
                        if (cfg.enabled) {
                            add(AppTileItem(title = stringResource(Res.string.adhan_sound), trailing = { Text(soundLabel(cfg.sound), fontWeight = FontWeight.Bold, color = c.primary) }, onClick = { soundFor = p.key }))
                            add(toggleTile(stringResource(Res.string.vibration), cfg.vibrate, { NotificationStore.setPrayerVibrate(p.key, it) }))
                            add(stepperTile(stringResource(Res.string.remind_before), cfg.remindBefore, min, { NotificationStore.setPrayerRemindBefore(p.key, it) }, N.Prayer.remindBeforeMin, N.Prayer.remindBeforeMax, N.Prayer.step))
                            add(toggleTile(stringResource(Res.string.at_prayer_time), cfg.atTime, { NotificationStore.setPrayerAtTime(p.key, it) }))
                            add(toggleTile(stringResource(Res.string.jamaat_reminder), cfg.jamaat, { NotificationStore.setPrayerJamaat(p.key, it) }))
                            if (cfg.jamaat) add(stepperTile(stringResource(Res.string.jamaat_after_start), cfg.jamaatAfter, min, { NotificationStore.setPrayerJamaatAfter(p.key, it) }, N.Prayer.jamaatAfterMin, N.Prayer.jamaatAfterMax, N.Prayer.step))
                        }
                    },
                )
            }

            val j = s.jumuah
            AppTileGroup(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                items = buildList {
                    add(AppTileItem(title = stringResource(Res.string.prayer_jumuah), subtitle = stringResource(Res.string.friday), leadingIcon = Lucide.Calendar, trailing = { AppSwitch(checked = j.enabled, onCheckedChange = NotificationStore::setJumuahEnabled) }))
                    if (j.enabled) {
                        add(stepperTile(stringResource(Res.string.remind_before), j.remindBefore, min, NotificationStore::setJumuahRemindBefore, N.Jumuah.remindBeforeMin, N.Jumuah.remindBeforeMax, N.Jumuah.step))
                        add(stepperTile(stringResource(Res.string.jamaat_after_start), j.jamaatAfter, min, NotificationStore::setJumuahJamaatAfter, N.Jumuah.jamaatAfterMin, N.Jumuah.jamaatAfterMax, N.Jumuah.step))
                        add(toggleTile(stringResource(Res.string.remind_if_missed), j.remindIfMissed, NotificationStore::setJumuahMissed))
                    }
                },
            )

            val mk = s.mulk
            AppTileGroup(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                items = buildList {
                    add(AppTileItem(title = stringResource(Res.string.surah_al_mulk), subtitle = stringResource(Res.string.after_isha), trailing = { AppSwitch(checked = mk.enabled, onCheckedChange = NotificationStore::setMulkEnabled) }))
                    if (mk.enabled) add(stepperTile(stringResource(Res.string.after_isha), mk.afterIsha, min, NotificationStore::setMulkAfter, N.Mulk.afterIshaMin, N.Mulk.afterIshaMax, N.Mulk.step))
                },
            )

            val kf = s.kahf
            AppTileGroup(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                items = buildList {
                    add(AppTileItem(title = stringResource(Res.string.surah_al_kahf), subtitle = stringResource(Res.string.friday), trailing = { AppSwitch(checked = kf.enabled, onCheckedChange = NotificationStore::setKahfEnabled) }))
                    if (kf.enabled) add(AppTileItem(title = stringResource(Res.string.reminder_time), trailing = { Text(LocalTime(kf.hour, kf.minute).format(timeFormat.pattern), fontWeight = FontWeight.Bold, color = c.primary) }, onClick = { kahfPicker = true }))
                },
            )

            NotifDhikr()

            AppTileGroup(items = listOf(toggleTile(stringResource(Res.string.tahajjud), s.nafil.tahajjud, NotificationStore::setTahajjud, stringResource(Res.string.last_third_of_the_night))))
            AppTileGroup(items = listOf(toggleTile(stringResource(Res.string.ishraq), s.nafil.ishraq, NotificationStore::setIshraq, stringResource(Res.string.mid_morning))))

            Spacer(Modifier.height(8.dp))
        }
    }

    soundFor?.let { key ->
        val current = s.prayers.getValue(key).sound
        AppBottomSheet(onDismiss = { soundFor = null }, title = stringResource(Res.string.adhan_sound)) {
            AppTileGroup(
                items = N.sounds.map { sound ->
                    AppTileItem(
                        title = soundLabel(sound),
                        selected = sound == current,
                        trailing = { if (sound == current) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp)) },
                        onClick = { NotificationStore.setPrayerSound(key, sound); soundFor = null },
                    )
                },
            )
        }
    }

    if (kahfPicker) {
        val state = rememberTimePickerState(initialHour = s.kahf.hour, initialMinute = s.kahf.minute, is24Hour = timeFormat == TimeFormat.TwentyFour)
        AppBottomSheet(onDismiss = { kahfPicker = false }, title = stringResource(Res.string.reminder_time)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TimePicker(state = state)
                AppButton(text = stringResource(Res.string.save), onClick = { NotificationStore.setKahfTime(state.hour, state.minute); kahfPicker = false }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** Canonical adhan-sound code → localized label (keeps the checkmark correct across a locale switch). */
@Composable
private fun soundLabel(value: String): String = when (value) {
    "Takbir" -> stringResource(Res.string.takbir)
    "Beep" -> stringResource(Res.string.beep)
    "Silent" -> stringResource(Res.string.silent)
    else -> stringResource(Res.string.full_adhan)
}

// ───────────────────────── tile builders ─────────────────────────

/** A tile whose trailing control is a switch. */
private fun toggleTile(title: String, checked: Boolean, onChange: (Boolean) -> Unit, subtitle: String? = null) = AppTileItem(
    title = title,
    subtitle = subtitle,
    trailing = { AppSwitch(checked = checked, onCheckedChange = onChange) },
)

/** A tile whose trailing control is a compact ± minute stepper. */
private fun stepperTile(title: String, value: Int, suffix: String, onChange: (Int) -> Unit, min: Int, max: Int, step: Int = 5) = AppTileItem(
    title = title,
    trailing = { MiniStepper(value, suffix, onChange, min, max, step) },
)
