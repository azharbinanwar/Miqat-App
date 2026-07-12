package com.example.miqatapp.feature.sandbox.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppColors
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.config.theme.ThemeMode
import com.example.miqatapp.config.theme.darkAppColors
import com.example.miqatapp.config.theme.lightAppColors
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.AppButtonSize
import com.example.miqatapp.core.components.AppButtonVariant
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.StateView
import com.example.miqatapp.core.enums.AdhanRoundingStyle
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.MiqatTimeStatus
import com.example.miqatapp.core.enums.PrayerTrackerStatus
import com.example.miqatapp.core.enums.color
import com.example.miqatapp.core.enums.onColor
import com.example.miqatapp.core.datetime.format
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource

/** Scratch page to eyeball every button and color in light + dark. */
@Composable
fun SandboxScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            FormatShowcase()
            Panel("LIGHT", ThemeMode.LIGHT)
            Panel("DARK", ThemeMode.DARK)
        }
    }
}

/** Renders the full showcase under a forced theme mode. */
@Composable
private fun Panel(title: String, mode: ThemeMode) {
    AppTheme(themeMode = mode) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(title)
            SectionTitle("Miqat — All (chronological)")
            MiqatAllShowcase()
            SectionTitle("Miqat — Groups")
            MiqatGroupsShowcase()
            SectionTitle("Tracker Status")
            TrackerStatusShowcase()
            SectionTitle("Time Status")
            TimeStatusShowcase()
            SectionTitle("Calc Methods")
            CalcMethodShowcase()
            SectionTitle("Config Enums")
            ConfigEnumsShowcase()
            SectionTitle("Tiles")
            TileShowcase()
            SectionTitle("Buttons")
            ButtonShowcase()
            SectionTitle("StateView")
            StateView(
                title = "No prayers logged",
                message = "Start tracking your prayers today.",
                action = { AppButton("Track now", {}, size = AppButtonSize.Small) },
            )
            SectionTitle("Colors")
            ColorPalette(if (mode == ThemeMode.DARK) darkAppColors() else lightAppColors())
        }
    }
}

/** Generic format(pattern) — one method, the pattern decides the output. */
@Composable
private fun FormatShowcase() {
    val sample = LocalDateTime(2026, 7, 12, 17, 8, 42)   // Sun 12 Jul 2026, 17:08:42
    val cases = listOf(
        "mm" to "just the minute",
        "HH" to "just the hour (24h)",
        "HH:mm" to "time",
        "h:mm a" to "time (12h)",
        "dd/MM/yyyy" to "date",
        "yyyy-MM-dd" to "date (ISO)",
        "HH:mm:ss" to "time w/ seconds",
        "dd/MM/yyyy HH:mm" to "date + time",
    )
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionTitle("Generic format(pattern)")
        Text("sample = $sample", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
        cases.forEach { (pattern, note) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("\"$pattern\"", modifier = Modifier.fillMaxWidth(0.42f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Text("→ ${sample.format(pattern)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
            Text(note, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun ButtonShowcase() {
    AppButtonVariant.entries.forEach { variant ->
        Text(variant.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AppButton("Large", {}, variant = variant, size = AppButtonSize.Large)
            AppButton("Med", {}, variant = variant, size = AppButtonSize.Medium)
            AppButton("Sm", {}, variant = variant, size = AppButtonSize.Small)
        }
    }
    Text("States", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        AppButton("Disabled", {}, enabled = false)
        AppButton("Loading", {}, isProcessing = true)
        AppButton("Icons", {}, leftIcon = { Text("★") }, rightIcon = { Text("→") })
    }
}

/** All 12 time points in day order — enum order IS the sort order. */
@Composable
private fun MiqatAllShowcase() {
    AppTileGroup(
        items = Miqat.entries.map { m ->
            AppTileItem(
                title = stringResource(m.labelRes),
                subtitle = m.category.name.lowercase() + if (m.isPrayer) " · tracked" else "",
                leading = {
                    Box(
                        Modifier.size(40.dp).background(m.color, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(m.icon, contentDescription = m.name, tint = m.onColor, modifier = Modifier.size(20.dp))
                    }
                },
                onClick = {},
            )
        },
    )
}

/** Same data sliced by category — what each screen would pick from. */
@Composable
private fun MiqatGroupsShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            "PRAYERS (Tracker, logging, notifications)" to Miqat.PRAYERS,
            "SOLAR (timeline extras: Sunrise/Ishraq/Zawal/Sunset)" to Miqat.SOLAR,
            "NIGHT (Midnight, Last third)" to Miqat.NIGHT,
            "RAMADAN (Imsak)" to Miqat.RAMADAN,
        ).forEach { (label, group) ->
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                group.forEach { m -> AccentChip(stringResource(m.labelRes), m.color, m.onColor, m.icon) }
            }
        }
    }
}

@Composable
private fun AccentChip(label: String, color: Color, onColor: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(40.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = onColor, modifier = Modifier.size(20.dp))
        }
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun TrackerStatusShowcase() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PrayerTrackerStatus.entries.forEach { AccentChip(it.label, it.color, it.onColor, it.icon) }
    }
}

@Composable
private fun TimeStatusShowcase() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiqatTimeStatus.entries.forEach { AccentChip(it.label, it.color, it.onColor, it.icon) }
    }
}

@Composable
private fun CalcMethodShowcase() {
    AppTileGroup(
        items = CalculationMethod.entries.map { m ->
            val isha = m.ishaAngle?.let { "Isha ${it}°" } ?: "Isha ${m.ishaIntervalMinutes} min"
            AppTileItem(title = m.label, subtitle = "${m.region} · Fajr ${m.fajrAngle}° · $isha")
        },
    )
}

@Composable
private fun ConfigEnumsShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val c = MaterialTheme.colorScheme.onBackground
        Text("Madhab: " + Madhab.entries.joinToString { it.label }, fontSize = 13.sp, color = c)
        Text("High-lat: " + HighLatRule.entries.joinToString { it.label }, fontSize = 13.sp, color = c)
        Text("Rounding: " + AdhanRoundingStyle.entries.joinToString { it.label }, fontSize = 13.sp, color = c)
    }
}

@Composable
private fun TileShowcase() {
    AppTileGroup(
        title = "General",
        items = listOf(
            AppTileItem(title = "Notifications", subtitle = "Before, at-time & Jamaat", onClick = {}),
            AppTileItem(title = "Calculation Method", subtitle = "Umm al-Qura", onClick = {}),
            AppTileItem(title = "Language", trailing = { Text("English") }, onClick = {}),
        ),
    )
    AppTileGroup(
        title = "Appearance",
        items = listOf(
            AppTileItem(title = "Theme", subtitle = "Dark", selected = true, onClick = {}),
            AppTileItem(title = "Primary Color", badge = { Text("New") }, onClick = {}),
        ),
    )
    AppTile(title = "About Miqat", subtitle = "Standalone tile", onClick = {})
}

@Composable
private fun ColorPalette(c: AppColors) {
    colorRows(c).forEach { (name, color) ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(52.dp).background(color, RoundedCornerShape(8.dp)))
            Text(name, modifier = Modifier.fillMaxWidth(0.45f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(color.toHex(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun SectionTitle(text: String) =
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)

private fun Color.toHex(): String =
    "#" + toArgb().toUInt().toString(16).padStart(8, '0').uppercase()

private fun colorRows(c: AppColors): List<Pair<String, Color>> = listOf(
    "primary" to c.primary, "onPrimary" to c.onPrimary,
    "primaryContainer" to c.primaryContainer, "onPrimaryContainer" to c.onPrimaryContainer,
    "secondary" to c.secondary, "onSecondary" to c.onSecondary,
    "error" to c.error, "onError" to c.onError,
    "success" to c.success, "successContainer" to c.successContainer,
    "info" to c.info, "infoContainer" to c.infoContainer,
    "warning" to c.warning, "warningContainer" to c.warningContainer,
    "scaffoldBackground" to c.scaffoldBackgroundColor, "card" to c.cardColor,
    "appbar" to c.appbarColor, "shadow" to c.shadow,
    "neutral" to c.neutral, "neutralContainer" to c.neutralContainer,
    "neutralVariant" to c.neutralVariant, "neutralMuted" to c.neutralMuted,
)
