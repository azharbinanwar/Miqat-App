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
import com.example.miqatapp.core.widgets.AppButton
import com.example.miqatapp.core.widgets.AppButtonSize
import com.example.miqatapp.core.widgets.AppButtonVariant
import com.example.miqatapp.core.widgets.AppTile
import com.example.miqatapp.core.widgets.AppTileGroup
import com.example.miqatapp.core.widgets.AppTileItem
import com.example.miqatapp.core.widgets.StateView

/** Scratch page to eyeball every button and color in light + dark. */
@Composable
fun SandboxScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            Panel("LIGHT", ThemeMode.LIGHT)
            Panel("DARK", ThemeMode.DARK)
        }
    }
}

/** Renders the full showcase under a forced theme mode. */
@Composable
private fun Panel(title: String, mode: ThemeMode) {
    AppTheme(themeMode = mode) {
        val c = AppTheme.colors
        Column(
            modifier = Modifier.fillMaxWidth().background(c.scaffoldBackgroundColor).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(title)
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
