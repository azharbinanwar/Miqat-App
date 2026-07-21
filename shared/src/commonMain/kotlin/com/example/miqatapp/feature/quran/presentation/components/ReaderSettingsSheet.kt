package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.AlignJustify
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ChevronsDown
import com.composables.icons.lucide.Languages
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.Type
import com.composables.icons.lucide.WholeWord
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.core.constants.defaults.QuranDefaults
import com.example.miqatapp.feature.quran.data.QuranFont
import org.jetbrains.compose.resources.Font

// global reader/display settings — opened from the app-bar icon (not ayah-specific).
// ponytail: all rows listed as reminders; the ones without a feature yet are placeholders (onClick = {}).
@Composable
fun ReaderSettingsSheet(
    fontSize: Int,
    onFontChange: (Int) -> Unit,
    font: QuranFont,
    onFontSelect: (QuranFont) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss, title = "Reading settings") {
        AppTileGroup(
            title = "Display",
            items = listOf(
                AppTileItem(
                    title = "Text size",
                    leadingIcon = Lucide.Type,
                    trailing = { MiniStepper(value = fontSize, suffix = "sp", onChange = onFontChange, min = QuranDefaults.MIN_FONT_SP, max = QuranDefaults.MAX_FONT_SP) },
                ),
                AppTileItem(title = "Line spacing", subtitle = "placeholder", leadingIcon = Lucide.AlignJustify, onClick = {}),
                AppTileItem(title = "Theme", subtitle = "Light · Dark · Sepia — placeholder", leadingIcon = Lucide.Palette, onClick = {}),
            ),
        )
        Text("Script", color = colors.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 6.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuranFont.entries.forEach { f -> FontChip(f, selected = f == font, onClick = { onFontSelect(f) }) }
        }
        Spacer(Modifier.size(12.dp))
        AppTileGroup(
            title = "Content",
            items = listOf(
                AppTileItem(title = "Translation", subtitle = "on/off + pick — placeholder", leadingIcon = Lucide.Languages, onClick = {}),
                AppTileItem(title = "Tafsir source", subtitle = "placeholder", leadingIcon = Lucide.BookOpen, onClick = {}),
                AppTileItem(title = "Word-by-word", subtitle = "on/off — placeholder", leadingIcon = Lucide.WholeWord, onClick = {}),
            ),
        )
        AppTileGroup(
            title = "Reading",
            items = listOf(
                AppTileItem(title = "Auto-scroll", subtitle = "play/pause + speed — placeholder", leadingIcon = Lucide.ChevronsDown, onClick = {}),
                AppTileItem(title = "Keep screen on", subtitle = "placeholder", leadingIcon = Lucide.Sun, onClick = {}),
                AppTileItem(title = "Jump to surah / juz / page", subtitle = "placeholder", leadingIcon = Lucide.Navigation, onClick = {}),
            ),
        )
    }
}

// live preview chip: the same Arabic sample in each font so the user picks by look, not name
@Composable
private fun FontChip(font: QuranFont, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val fam = FontFamily(Font(font.res))
    Column(
        Modifier.clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(1.dp, if (selected) colors.primary else colors.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(font.sample, fontFamily = fam, fontSize = 20.sp, color = colors.onBackground, maxLines = 1)
        Spacer(Modifier.size(3.dp))
        Text(font.label, color = if (selected) colors.primary else colors.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
    }
}
