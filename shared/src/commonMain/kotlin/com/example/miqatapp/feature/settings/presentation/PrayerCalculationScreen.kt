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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.widgets.AppBottomSheet
import com.example.miqatapp.core.widgets.AppTileGroup
import com.example.miqatapp.core.widgets.AppTileItem
import com.example.miqatapp.core.widgets.MiniStepper
import com.example.miqatapp.feature.prayer.domain.PrayerCalculationRepository
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.asr_method
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.calculation_method
import com.example.miqatapp.resources.fajr_angle
import com.example.miqatapp.resources.high_latitude_rule
import com.example.miqatapp.resources.isha_angle
import com.example.miqatapp.resources.manual_adjustments
import com.example.miqatapp.resources.minutes_short
import com.example.miqatapp.resources.prayer_calculation
import org.jetbrains.compose.resources.stringResource

/**
 * Prayer calculation — three enum-backed choices (method / Asr madhab / high-latitude rule), each opened as
 * a bottom-sheet list of the enum's entries. Picking "Custom" reveals two angle steppers. Persisted via
 * [Prefs]. ponytail: angles are whole degrees for now (real methods use fractions like 18.5°).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerCalculationScreen(onBack: () -> Unit = {}) {
    // Single source of truth — repo resolves Prefs ?: MiqatDefaults; writes flow back through its setters.
    val repo = PrayerCalculationRepository
    val method by repo.method.collectAsState()
    val madhab by repo.madhab.collectAsState()
    val highLat by repo.highLatRule.collectAsState()
    val fajrAngle by repo.fajrAngle.collectAsState()
    val ishaAngle by repo.ishaAngle.collectAsState()
    val adjust by repo.adjustments.collectAsState()

    var showMethod by remember { mutableStateOf(false) }
    var showMadhab by remember { mutableStateOf(false) }
    var showHighLat by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.prayer_calculation)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
            )
        },
    ) { pad ->
        val minLabel = stringResource(Res.string.minutes_short)
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            AppTileGroup(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                // Custom reveals its angle steppers inline, right under the method row
                items = buildList {
                    add(AppTileItem(title = stringResource(Res.string.calculation_method), subtitle = method.label, onClick = { showMethod = true }))
                    if (method == CalculationMethod.Custom) {
                        add(AppTileItem(title = stringResource(Res.string.fajr_angle), trailing = { MiniStepper(fajrAngle, "°", { repo.setFajrAngle(it) }, min = 10, max = 21) }))
                        add(AppTileItem(title = stringResource(Res.string.isha_angle), trailing = { MiniStepper(ishaAngle, "°", { repo.setIshaAngle(it) }, min = 10, max = 21) }))
                    }
                    add(AppTileItem(title = stringResource(Res.string.asr_method), subtitle = madhab.label, onClick = { showMadhab = true }))
                    add(AppTileItem(title = stringResource(Res.string.high_latitude_rule), subtitle = highLat.label, onClick = { showHighLat = true }))
                },
            )

            Spacer(Modifier.height(8.dp))
            AppTileGroup(
                title = stringResource(Res.string.manual_adjustments),
                items = Miqat.DAILY.map { p ->
                    AppTileItem(
                        title = stringResource(p.labelRes),
                        leadingIcon = p.icon,
                        trailing = { MiniStepper(adjust[p] ?: 0, minLabel, { repo.setAdjustment(p, it) }, min = -30, max = 30) },
                    )
                },
            )
        }
    }

    if (showMethod) PickerSheet(stringResource(Res.string.calculation_method), CalculationMethod.entries, method, { it.label }, { it.region }, { repo.setMethod(it); showMethod = false }) { showMethod = false }
    if (showMadhab) PickerSheet(stringResource(Res.string.asr_method), Madhab.entries, madhab, { it.label }, onPick = { repo.setMadhab(it); showMadhab = false }, onDismiss = { showMadhab = false })
    if (showHighLat) PickerSheet(stringResource(Res.string.high_latitude_rule), HighLatRule.entries, highLat, { it.label }, onPick = { repo.setHighLatRule(it); showHighLat = false }, onDismiss = { showHighLat = false })
}

/**
 * A fixed-set picker in a bottom sheet: enum entries → tiles → sheet. Takes plain `label`/`sublabel` lambdas
 * (any enum works — no marker interface needed); the selected row shows a check.
 */
@Composable
private fun <T> PickerSheet(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    sublabel: ((T) -> String)? = null,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss, title = title) {
        AppTileGroup(
            items = options.map { opt ->
                val sel = opt == selected
                AppTileItem(
                    title = label(opt),
                    subtitle = sublabel?.invoke(opt),
                    selected = sel,
                    trailing = { if (sel) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp)) },
                    onClick = { onPick(opt) },
                )
            },
        )
    }
}
