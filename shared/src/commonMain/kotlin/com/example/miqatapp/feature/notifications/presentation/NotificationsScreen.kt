package com.example.miqatapp.feature.notifications.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.SunMedium
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
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.feature.notifications.presentation.components.NotificationDhikr
import com.example.miqatapp.feature.notifications.presentation.components.NotificationTestScreen
import com.example.miqatapp.feature.notifications.presentation.components.NotificationsNeedsAttention
import com.example.miqatapp.feature.notifications.store.NotificationStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.after_isha
import com.example.miqatapp.resources.all_alerts
import com.example.miqatapp.resources.at_prayer_time
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.friday
import com.example.miqatapp.resources.ishraq
import com.example.miqatapp.resources.jamaat_after_start
import com.example.miqatapp.resources.jamaat_reminder
import com.example.miqatapp.resources.last_third_of_the_night
import com.example.miqatapp.resources.master_switch_for_every_reminder
import com.example.miqatapp.resources.mid_morning
import com.example.miqatapp.resources.minutes_before
import com.example.miqatapp.resources.minutes_short
import com.example.miqatapp.resources.notif_at_before
import com.example.miqatapp.resources.notif_at_jamaah
import com.example.miqatapp.resources.notifications
import com.example.miqatapp.resources.prayer_jumuah
import com.example.miqatapp.resources.remind_before
import com.example.miqatapp.resources.reminder_time
import com.example.miqatapp.resources.save
import com.example.miqatapp.resources.surah_al_kahf
import com.example.miqatapp.resources.surah_al_mulk
import com.example.miqatapp.resources.notif_verse_sub
import com.example.miqatapp.resources.tahajjud
import com.example.miqatapp.resources.verse_of_the_day
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
    val today by MiqatTimesStore.today.collectAsState() // for the fire-time preview under each tile

    // UI-only: whether the Kahf time picker is open.
    var kahfPicker by remember { mutableStateOf(false) }
    var taps by remember { mutableStateOf(0) }        // 7 taps on "All alerts" opens the dev test screen
    var showTest by remember { mutableStateOf(false) }
    var verseOfDay by remember { mutableStateOf(false) } // ponytail: UI shell only, not stored/scheduled yet
    if (showTest) { NotificationTestScreen(onBack = { showTest = false }); return }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.notifications)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
            )
        },
    ) { pad ->
        val min = stringResource(Res.string.minutes_short)
        val pat = timeFormat.pattern
        val lBefore = stringResource(Res.string.notif_at_before)
        val lJamaah = stringResource(Res.string.notif_at_jamaah)
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            NotificationsNeedsAttention() // red tile at top until notifications are granted

            // Wrapped in a no-ripple gesture: 7 taps on the row opens the dev test sheet. The switch still works.
            Box(Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { taps++; if (taps >= 7) showTest = true }) {
                AppTileGroup(items = listOf(toggleTile(stringResource(Res.string.all_alerts), s.allAlerts, NotificationStore::setAllAlerts, stringResource(Res.string.master_switch_for_every_reminder))))
            }

            // Master off collapses the list (nothing fires); states stay saved, so turning it back on
            // restores the setup and re-books only the enabled ones.
            if (s.allAlerts) {
            // One group per prayer: toggle, then its settings on enable.
            Miqat.PRAYERS.forEach { p ->
                val cfg = s.prayers.getValue(p.key)
                val base = timeOf(today, p)
                val label = stringResource(p.labelRes)
                AppTileGroup(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    items = buildList {
                        add(AppTileItem(
                            title = if (base != null) "$label · ${base.format(pat)}" else label,
                            subtitle = if (cfg.enabled) offsetSubtitle(base, if (cfg.remindBeforeOn) cfg.remindBefore else 0, cfg.jamaat, cfg.jamaatAfter, lBefore, lJamaah, pat) else null,
                            leadingIcon = p.icon,
                            trailing = { AppSwitch(checked = cfg.enabled, onCheckedChange = { NotificationStore.setPrayerEnabled(p.key, it) }) },
                        ))
                        if (cfg.enabled) {
                            add(toggleTile(stringResource(Res.string.at_prayer_time), cfg.atTime, { NotificationStore.setPrayerAtTime(p.key, it) }))
                            add(toggleTile(stringResource(Res.string.remind_before), cfg.remindBeforeOn, { NotificationStore.setPrayerRemindBeforeOn(p.key, it) }))
                            if (cfg.remindBeforeOn) add(stepperTile(stringResource(Res.string.minutes_before), cfg.remindBefore, min, { NotificationStore.setPrayerRemindBefore(p.key, it) }, N.Prayer.remindBeforeMin, N.Prayer.remindBeforeMax, N.Prayer.step))
                            add(toggleTile(stringResource(Res.string.jamaat_reminder), cfg.jamaat, { NotificationStore.setPrayerJamaat(p.key, it) }))
                            if (cfg.jamaat) add(stepperTile(stringResource(Res.string.jamaat_after_start), cfg.jamaatAfter, min, { NotificationStore.setPrayerJamaatAfter(p.key, it) }, N.Prayer.jamaatAfterMin, N.Prayer.jamaatAfterMax, N.Prayer.step))
                        }
                    },
                )
            }

            val j = s.jumuah
            val jBase = timeOf(today, Miqat.Dhuhr) // Jumu'ah rides Friday's Dhuhr time
            val jLabel = stringResource(Res.string.prayer_jumuah)
            AppTileGroup(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                items = buildList {
                    add(AppTileItem(
                        title = if (jBase != null) "$jLabel · ${jBase.format(pat)}" else jLabel,
                        subtitle = if (j.enabled) offsetSubtitle(jBase, if (j.remindBeforeOn) j.remindBefore else 0, j.jamaat, j.jamaatAfter, lBefore, lJamaah, pat) else stringResource(Res.string.friday),
                        leadingIcon = Lucide.Calendar,
                        trailing = { AppSwitch(checked = j.enabled, onCheckedChange = NotificationStore::setJumuahEnabled) },
                    ))
                    if (j.enabled) {
                        add(toggleTile(stringResource(Res.string.remind_before), j.remindBeforeOn, NotificationStore::setJumuahRemindBeforeOn))
                        if (j.remindBeforeOn) add(stepperTile(stringResource(Res.string.minutes_before), j.remindBefore, min, NotificationStore::setJumuahRemindBefore, N.Jumuah.remindBeforeMin, N.Jumuah.remindBeforeMax, N.Jumuah.step))
                        add(toggleTile(stringResource(Res.string.jamaat_reminder), j.jamaat, NotificationStore::setJumuahJamaat))
                        if (j.jamaat) add(stepperTile(stringResource(Res.string.jamaat_after_start), j.jamaatAfter, min, NotificationStore::setJumuahJamaatAfter, N.Jumuah.jamaatAfterMin, N.Jumuah.jamaatAfterMax, N.Jumuah.step))
                    }
                },
            )

            val mk = s.mulk
            AppTileGroup(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                items = buildList {
                    add(AppTileItem(title = stringResource(Res.string.surah_al_mulk), subtitle = timeSubtitle(stringResource(Res.string.after_isha), shift(timeOf(today, Miqat.Isha), mk.afterIsha), mk.enabled, pat), leadingIcon = Lucide.BookOpen, trailing = { AppSwitch(checked = mk.enabled, onCheckedChange = NotificationStore::setMulkEnabled) }))
                    if (mk.enabled) add(stepperTile(stringResource(Res.string.after_isha), mk.afterIsha, min, NotificationStore::setMulkAfter, N.Mulk.afterIshaMin, N.Mulk.afterIshaMax, N.Mulk.step))
                },
            )

            val kf = s.kahf
            AppTileGroup(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                items = buildList {
                    add(AppTileItem(title = stringResource(Res.string.surah_al_kahf), subtitle = timeSubtitle(stringResource(Res.string.friday), LocalTime(kf.hour, kf.minute), kf.enabled, pat), leadingIcon = Lucide.BookOpen, trailing = { AppSwitch(checked = kf.enabled, onCheckedChange = NotificationStore::setKahfEnabled) }))
                    if (kf.enabled) add(AppTileItem(title = stringResource(Res.string.reminder_time), trailing = { Text(LocalTime(kf.hour, kf.minute).format(timeFormat.pattern), fontWeight = FontWeight.Bold, color = c.primary) }, onClick = { kahfPicker = true }))
                },
            )

            NotificationDhikr()

            AppTileGroup(items = listOf(toggleTile(stringResource(Res.string.tahajjud), s.nafil.tahajjud, NotificationStore::setTahajjud, timeSubtitle(stringResource(Res.string.last_third_of_the_night), timeOf(today, Miqat.LastThird), s.nafil.tahajjud, pat), icon = Lucide.Moon)))
            AppTileGroup(items = listOf(toggleTile(stringResource(Res.string.ishraq), s.nafil.ishraq, NotificationStore::setIshraq, timeSubtitle(stringResource(Res.string.mid_morning), timeOf(today, Miqat.Ishraq), s.nafil.ishraq, pat), icon = Lucide.SunMedium)))

            // Ayah of the day — shell only for now (local state), wired to a real store/scheduler later.
            AppTileGroup(items = listOf(toggleTile(stringResource(Res.string.verse_of_the_day), verseOfDay, { verseOfDay = it }, stringResource(Res.string.notif_verse_sub), icon = Lucide.Sparkles)))
            }

            Spacer(Modifier.height(8.dp))
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

// ───────────────────────── tile builders ─────────────────────────

/** A tile whose trailing control is a switch. */
private fun toggleTile(title: String, checked: Boolean, onChange: (Boolean) -> Unit, subtitle: String? = null, icon: ImageVector? = null) = AppTileItem(
    title = title,
    subtitle = subtitle,
    leadingIcon = icon,
    trailing = { AppSwitch(checked = checked, onCheckedChange = onChange) },
)

/** A tile whose trailing control is a compact ± minute stepper. */
private fun stepperTile(title: String, value: Int, suffix: String, onChange: (Int) -> Unit, min: Int, max: Int, step: Int = 5) = AppTileItem(
    title = title,
    trailing = { MiniStepper(value, suffix, onChange, min, max, step) },
)
