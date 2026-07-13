package com.example.miqatapp.feature.settings.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.VolumeX
import com.composables.icons.lucide.Zap
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.constants.defaults.FocusDefaults
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.focus.PhoneSilencer
import com.example.miqatapp.core.focus.SilenceMode
import com.example.miqatapp.core.focus.rememberFocusSetup
import com.example.miqatapp.core.permissions.AppPermission
import com.example.miqatapp.core.permissions.PermissionDeniedSheet
import com.example.miqatapp.core.permissions.PermissionStatus
import com.example.miqatapp.core.permissions.rememberPermissionService
import com.example.miqatapp.core.store.PrayerFocusStore
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.silence_phone_around_each_prayer_set_separately
import com.example.miqatapp.resources.focus_allow_background
import com.example.miqatapp.resources.focus_allow_background_sub
import com.example.miqatapp.resources.focus_needs_attention
import com.example.miqatapp.resources.focus_notif_needed
import com.example.miqatapp.resources.focus_notif_sub
import com.example.miqatapp.resources.focus_dnd_needed
import com.example.miqatapp.resources.focus_dnd_sub
import com.example.miqatapp.resources.focus_notif_denied_title
import com.example.miqatapp.resources.focus_notif_denied_msg
import com.example.miqatapp.resources.silence_for
import com.example.miqatapp.resources.silence_mode
import com.example.miqatapp.resources.start_after
import com.example.miqatapp.resources.prayer_focus
import com.example.miqatapp.resources.minutes_short
import org.jetbrains.compose.resources.stringResource
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.feature.settings.presentation.components.FocusTestSheet
import com.example.miqatapp.feature.settings.presentation.components.ModeToggle

// "4:40–5:10" window from a prayer time + the offsets; the mode label is prepended at the call site.
private fun windowRange(prayer: LocalTime, after: Int, duration: Int, pattern: String): String {
    val start = LocalTime.fromSecondOfDay((prayer.toSecondOfDay() + after * 60) % 86400)
    val end = LocalTime.fromSecondOfDay((prayer.toSecondOfDay() + (after + duration) * 60) % 86400)
    return "${start.format(pattern)}–${end.format(pattern)}"
}

// Prayer Focus (Android only): mute the phone around each prayer. Settings + test path for now;
// wiring the mute service into the real schedule is the next step.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerFocusScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val today by MiqatTimesStore.today.collectAsState()
    val configs by PrayerFocusStore.configs.collectAsState()
    val setup = rememberFocusSetup()
    val perms = rememberPermissionService()
    val scope = rememberCoroutineScope()
    var showTest by remember { mutableStateOf(false) }
    var taps by remember { mutableStateOf(0) } // 7 taps on the blurb reveals the test tiles; resets on re-entry
    var showNotifDenied by remember { mutableStateOf(false) }
    // poll so the "Needs attention" rows refresh when the user returns from a settings screen
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1500); tick++ } }
    val battOk = remember(tick) { setup.batteryUnrestricted() }
    val dndOk = remember(tick) { setup.hasSilenceAccess() }
    var notifOk by remember { mutableStateOf(true) }
    LaunchedEffect(tick) { notifOk = perms.status(AppPermission.Notifications) == PermissionStatus.Granted }

    if (showNotifDenied) PermissionDeniedSheet(
        title = stringResource(Res.string.focus_notif_denied_title),
        message = stringResource(Res.string.focus_notif_denied_msg),
        onOpenSettings = { showNotifDenied = false; perms.openAppSettings() },
        onDismiss = { showNotifDenied = false },
    )

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
                fontSize = 13.sp, color = c.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp).clickable { taps++ },
            )
            Spacer(Modifier.height(12.dp))
            // Warning rows for anything missing; the whole section disappears once everything's granted.
            if (setup.supported) {
                val anySilent = configs.values.any { it.enabled && it.mode == SilenceMode.Silent }
                // Required + reliably checkable go in the red "Needs attention" section.
                val warnings = buildList {
                    if (!notifOk) add(AppTileItem(
                        title = stringResource(Res.string.focus_notif_needed),
                        subtitle = stringResource(Res.string.focus_notif_sub),
                        leadingIcon = Lucide.BellOff, leadingColor = c.error,
                        onClick = {
                            scope.launch {
                                if (perms.request(AppPermission.Notifications) == PermissionStatus.DeniedPermanently) showNotifDenied = true
                                tick++
                            }
                        },
                    ))
                    if (anySilent && !dndOk) add(AppTileItem(
                        title = stringResource(Res.string.focus_dnd_needed),
                        subtitle = stringResource(Res.string.focus_dnd_sub),
                        leadingIcon = Lucide.VolumeX, leadingColor = c.error,
                        onClick = { setup.requestSilenceAccess() },
                    ))
                }
                if (warnings.isNotEmpty()) {
                    AppTileGroup(modifier = Modifier.fillMaxWidth(), title = stringResource(Res.string.focus_needs_attention), items = warnings)
                    Spacer(Modifier.height(12.dp))
                }
                // Battery is optional (feature works without it, and the flag is unreliable on some OEMs) -> plain nudge, not red.
                if (!battOk) {
                    AppTileGroup(
                        modifier = Modifier.fillMaxWidth(),
                        items = listOf(AppTileItem(
                            title = stringResource(Res.string.focus_allow_background),
                            subtitle = stringResource(Res.string.focus_allow_background_sub),
                            leadingIcon = Lucide.Zap,
                            onClick = { setup.requestBatteryUnrestricted() },
                        )),
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (taps >= 7) {
            AppTileGroup(
                modifier = Modifier.fillMaxWidth(),
                items = listOf(
                    AppTileItem(
                        title = "Test now (mute 5s)",
                        subtitle = "Mutes for 5 seconds, then restores",
                        leadingIcon = Lucide.BellOff,
                        onClick = { PhoneSilencer.silenceFor(5000) },
                    ),
                    AppTileItem(
                        title = "Background test",
                        subtitle = "Pick a time, kill the app, confirm it mutes and restores on its own",
                        leadingIcon = Lucide.Clock,
                        onClick = { showTest = true },
                    ),
                ),
            )
            Spacer(Modifier.height(12.dp))
            }
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
                            subtitle = if (cfg.enabled && prayerTime != null) "${cfg.mode.label()} · ${windowRange(prayerTime, cfg.startAfter, cfg.duration, timeFormat.pattern)}" else null,
                            leadingIcon = row.miqat.icon,
                            trailing = { Switch(checked = cfg.enabled, onCheckedChange = { PrayerFocusStore.setEnabled(row.key, it) }) },
                        ))
                        if (cfg.enabled) {
                            add(AppTileItem(title = stringResource(Res.string.start_after), trailing = { MiniStepper(cfg.startAfter, min, { PrayerFocusStore.setStartAfter(row.key, it) }, min = 0, max = row.default.max) }))
                            add(AppTileItem(title = stringResource(Res.string.silence_for), trailing = { MiniStepper(cfg.duration, min, { PrayerFocusStore.setDuration(row.key, it) }, min = 5, max = row.default.max) }))
                            add(AppTileItem(title = stringResource(Res.string.silence_mode), trailing = {
                                ModeToggle(cfg.mode) { m ->
                                    if (m == SilenceMode.Silent && !setup.hasSilenceAccess()) setup.requestSilenceAccess()
                                    PrayerFocusStore.setMode(row.key, m)
                                }
                            }))
                        }
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        if (showTest) FocusTestSheet(onDismiss = { showTest = false })
    }
}
