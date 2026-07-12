package com.example.miqatapp.feature.settings.presentation

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.enums.ReminderPrayer
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.a_gentle_reminder_to_count
import com.example.miqatapp.resources.adhan_sound
import com.example.miqatapp.resources.after_asr
import com.example.miqatapp.resources.after_fajr
import com.example.miqatapp.resources.after_isha
import com.example.miqatapp.resources.all_alerts
import com.example.miqatapp.resources.allow_snooze
import com.example.miqatapp.resources.at_prayer_time
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.beep
import com.example.miqatapp.resources.behaviour
import com.example.miqatapp.resources.daily_tasbih_nudge
import com.example.miqatapp.resources.dhikr
import com.example.miqatapp.resources.ishraq
import com.example.miqatapp.resources.each_prayer
import com.example.miqatapp.resources.evening_adhkar
import com.example.miqatapp.resources.friday
import com.example.miqatapp.resources.friday_jumuah
import com.example.miqatapp.resources.full_adhan
import com.example.miqatapp.resources.i_prayed_quick_action
import com.example.miqatapp.resources.jamaat_after_start
import com.example.miqatapp.resources.jamaat_reminder
import com.example.miqatapp.resources.last_third_of_the_night
import com.example.miqatapp.resources.log_a_prayer_from_the_notification
import com.example.miqatapp.resources.master_switch_for_every_reminder
import com.example.miqatapp.resources.mid_morning
import com.example.miqatapp.resources.minutes_short
import com.example.miqatapp.resources.morning_adhkar
import com.example.miqatapp.resources.notifications
import com.example.miqatapp.resources.remind_before
import com.example.miqatapp.resources.remind_if_missed
import com.example.miqatapp.resources.respect_do_not_disturb
import com.example.miqatapp.resources.silent
import com.example.miqatapp.resources.sunnah_reminders
import com.example.miqatapp.resources.surah_al_kahf
import com.example.miqatapp.resources.surah_al_mulk
import com.example.miqatapp.resources.tahajjud
import com.example.miqatapp.resources.takbir
import com.example.miqatapp.resources.vibration
import org.jetbrains.compose.resources.stringResource
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.core.components.AppTileItem

/** Per-prayer alert state (mock, UI-first). Enabling reveals its settings — no separate expand control. */
private class PrayerAlert(val rp: ReminderPrayer) {
    var enabled by mutableStateOf(false)
    var sound by mutableStateOf("Full adhan")
    var vibrate by mutableStateOf(true)
    var remindBefore by mutableStateOf(15)
    var atTime by mutableStateOf(true)
    var jamaat by mutableStateOf(false)
    var jamaatAfter by mutableStateOf(20)
}

private val SOUND_OPTIONS = listOf("Full adhan", "Takbir", "Beep", "Silent")

/**
 * Notifications — the richest Settings module, built on tiles. A prayer is one tile with a toggle; flipping
 * it on grows the same group with its settings (sound, vibration, remind-before, jamaat). Then Jumu'ah,
 * Sunnah reminders, a dhikr nudge, and behaviour toggles. ponytail: all mock until scheduler + prefs land.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    var allAlerts by remember { mutableStateOf(true) }
    val prayers = remember { ReminderPrayer.daily.map { PrayerAlert(it) } }
    var soundFor by remember { mutableStateOf<PrayerAlert?>(null) }

    // Jumu'ah
    var jumuahBefore by remember { mutableStateOf(30) }
    var jumuahJamaat by remember { mutableStateOf(20) }
    var jumuahMissed by remember { mutableStateOf(true) }
    // Sunnah
    var mulk by remember { mutableStateOf(true) }
    var mulkAfter by remember { mutableStateOf(30) }
    var kahf by remember { mutableStateOf(true) }
    var tahajjud by remember { mutableStateOf(false) }
    var ishraq by remember { mutableStateOf(false) }
    // Dhikr
    var morningAdhkar by remember { mutableStateOf(true) }
    var eveningAdhkar by remember { mutableStateOf(true) }
    var tasbihNudge by remember { mutableStateOf(false) }
    // Behaviour
    var respectDnd by remember { mutableStateOf(true) }
    var snooze by remember { mutableStateOf(true) }
    var iPrayed by remember { mutableStateOf(true) }

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
            AppTileGroup(items = listOf(toggleTile(stringResource(Res.string.all_alerts), allAlerts, { allAlerts = it }, stringResource(Res.string.master_switch_for_every_reminder))))

            SectionLabel(stringResource(Res.string.each_prayer))
            prayers.forEach { p ->
                AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    items = buildList {
                        add(AppTileItem(title = stringResource(p.rp.labelRes), leadingIcon = p.rp.icon, trailing = { Switch(checked = p.enabled, onCheckedChange = { p.enabled = it }) }))
                        if (p.enabled) {
                            add(AppTileItem(title = stringResource(Res.string.adhan_sound), trailing = { Text(soundLabel(p.sound), fontWeight = FontWeight.Bold, color = c.primary) }, onClick = { soundFor = p }))
                            add(toggleTile(stringResource(Res.string.vibration), p.vibrate, { p.vibrate = it }))
                            add(stepperTile(stringResource(Res.string.remind_before), p.remindBefore, min, { p.remindBefore = it }, 0, 60))
                            add(toggleTile(stringResource(Res.string.at_prayer_time), p.atTime, { p.atTime = it }))
                            add(toggleTile(stringResource(Res.string.jamaat_reminder), p.jamaat, { p.jamaat = it }))
                            if (p.jamaat) add(stepperTile(stringResource(Res.string.jamaat_after_start), p.jamaatAfter, min, { p.jamaatAfter = it }, 5, 60))
                        }
                    },
                )
            }

            AppTileGroup(
                title = stringResource(Res.string.friday_jumuah),
                items = listOf(
                    stepperTile(stringResource(Res.string.remind_before), jumuahBefore, min, { jumuahBefore = it }, 15, 120),
                    stepperTile(stringResource(Res.string.jamaat_after_start), jumuahJamaat, min, { jumuahJamaat = it }, 5, 60),
                    toggleTile(stringResource(Res.string.remind_if_missed), jumuahMissed, { jumuahMissed = it }),
                ),
            )

            AppTileGroup(
                title = stringResource(Res.string.sunnah_reminders),
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                items = buildList {
                    add(toggleTile(stringResource(Res.string.surah_al_mulk), mulk, { mulk = it }, stringResource(Res.string.after_isha)))
                    if (mulk) add(stepperTile(stringResource(Res.string.after_isha), mulkAfter, min, { mulkAfter = it }, 5, 120))
                    add(toggleTile(stringResource(Res.string.surah_al_kahf), kahf, { kahf = it }, stringResource(Res.string.friday)))
                    add(toggleTile(stringResource(Res.string.tahajjud), tahajjud, { tahajjud = it }, stringResource(Res.string.last_third_of_the_night)))
                    add(toggleTile(stringResource(Res.string.ishraq), ishraq, { ishraq = it }, stringResource(Res.string.mid_morning)))
                },
            )

            AppTileGroup(
                title = stringResource(Res.string.dhikr),
                items = listOf(
                    toggleTile(stringResource(Res.string.morning_adhkar), morningAdhkar, { morningAdhkar = it }, stringResource(Res.string.after_fajr)),
                    toggleTile(stringResource(Res.string.evening_adhkar), eveningAdhkar, { eveningAdhkar = it }, stringResource(Res.string.after_asr)),
                    toggleTile(stringResource(Res.string.daily_tasbih_nudge), tasbihNudge, { tasbihNudge = it }, stringResource(Res.string.a_gentle_reminder_to_count)),
                ),
            )

            AppTileGroup(
                title = stringResource(Res.string.behaviour),
                items = listOf(
                    toggleTile(stringResource(Res.string.respect_do_not_disturb), respectDnd, { respectDnd = it }),
                    toggleTile(stringResource(Res.string.allow_snooze), snooze, { snooze = it }),
                    toggleTile(stringResource(Res.string.i_prayed_quick_action), iPrayed, { iPrayed = it }, stringResource(Res.string.log_a_prayer_from_the_notification)),
                ),
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    soundFor?.let { p ->
        AppBottomSheet(onDismiss = { soundFor = null }, title = stringResource(Res.string.adhan_sound)) {
            AppTileGroup(
                items = SOUND_OPTIONS.map { sound ->
                    AppTileItem(
                        title = soundLabel(sound),
                        selected = sound == p.sound,
                        trailing = { if (sound == p.sound) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp)) },
                        onClick = { p.sound = sound; soundFor = null },
                    )
                },
            )
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
    trailing = { Switch(checked = checked, onCheckedChange = onChange) },
)

/** A tile whose trailing control is a compact ± minute stepper. */
private fun stepperTile(title: String, value: Int, suffix: String, onChange: (Int) -> Unit, min: Int, max: Int, step: Int = 5) = AppTileItem(
    title = title,
    trailing = { MiniStepper(value, suffix, onChange, min, max, step) },
)

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AppTheme.colors.primary, modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp))
}

