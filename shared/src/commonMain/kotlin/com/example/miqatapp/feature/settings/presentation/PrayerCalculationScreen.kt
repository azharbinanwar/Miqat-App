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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Prayer
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.prefs.PrefKeys
import com.example.miqatapp.core.prefs.Prefs
import com.example.miqatapp.core.widgets.AppBottomSheet
import com.example.miqatapp.core.widgets.AppTileGroup
import com.example.miqatapp.core.widgets.AppTileItem
import com.example.miqatapp.core.widgets.MiniStepper
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
    val c = AppTheme.colors
    val method = CalculationMethod.fromName(Prefs.calcMethod)
    val madhab = Madhab.fromName(Prefs.madhab)
    val highLat = HighLatRule.fromName(Prefs.highLatRule)
    var fajrAngle by remember { mutableStateOf(Prefs.getInt(PrefKeys.CUSTOM_FAJR_ANGLE, 18)) }
    var ishaAngle by remember { mutableStateOf(Prefs.getInt(PrefKeys.CUSTOM_ISHA_ANGLE, 17)) }
    // per-prayer ± minute tweak (local mosque / observed sighting differs from the computed time)
    val adjust = remember { mutableStateMapOf<Prayer, Int>().apply { Prayer.entries.forEach { put(it, Prefs.getInt(PrefKeys.adjust(it.name), 0)) } } }

    var showMethod by remember { mutableStateOf(false) }
    var showMadhab by remember { mutableStateOf(false) }
    var showHighLat by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.prayer_calculation), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
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
                        add(AppTileItem(title = stringResource(Res.string.fajr_angle), trailing = { MiniStepper(fajrAngle, "°", { fajrAngle = it; Prefs.putInt(PrefKeys.CUSTOM_FAJR_ANGLE, it) }, min = 10, max = 21) }))
                        add(AppTileItem(title = stringResource(Res.string.isha_angle), trailing = { MiniStepper(ishaAngle, "°", { ishaAngle = it; Prefs.putInt(PrefKeys.CUSTOM_ISHA_ANGLE, it) }, min = 10, max = 21) }))
                    }
                    add(AppTileItem(title = stringResource(Res.string.asr_method), subtitle = madhab.label, onClick = { showMadhab = true }))
                    add(AppTileItem(title = stringResource(Res.string.high_latitude_rule), subtitle = highLat.label, onClick = { showHighLat = true }))
                },
            )

            Spacer(Modifier.height(8.dp))
            AppTileGroup(
                title = stringResource(Res.string.manual_adjustments),
                items = Prayer.entries.map { p ->
                    AppTileItem(
                        title = stringResource(p.labelRes),
                        leadingIcon = p.icon,
                        trailing = { MiniStepper(adjust.getValue(p), minLabel, { adjust[p] = it; Prefs.putInt(PrefKeys.adjust(p.name), it) }, min = -30, max = 30) },
                    )
                },
            )
        }
    }

    if (showMethod) PickerSheet(stringResource(Res.string.calculation_method), CalculationMethod.entries, method, { it.label }, { it.region }, { Prefs.calcMethod = it.name; showMethod = false }) { showMethod = false }
    if (showMadhab) PickerSheet(stringResource(Res.string.asr_method), Madhab.entries, madhab, { it.label }, onPick = { Prefs.madhab = it.name; showMadhab = false }, onDismiss = { showMadhab = false })
    if (showHighLat) PickerSheet(stringResource(Res.string.high_latitude_rule), HighLatRule.entries, highLat, { it.label }, onPick = { Prefs.highLatRule = it.name; showHighLat = false }, onDismiss = { showHighLat = false })
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
