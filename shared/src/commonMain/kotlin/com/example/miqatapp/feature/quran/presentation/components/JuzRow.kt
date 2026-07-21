package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.TilePosition
import com.example.miqatapp.feature.quran.data.Juz
import com.example.miqatapp.feature.quran.data.Surah
import com.example.miqatapp.feature.quran.toJuzKey
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_juz
import org.jetbrains.compose.resources.Font

// self-contained juz: collapsed shows one tile; tapping the chevron folds its surahs in beneath.
// Owns its expanded state and reads juz.surahs, so the screen just hands it the juz.
@Composable
fun JuzRow(juz: Juz, onOpen: () -> Unit, onOpenSurah: (Surah) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val nameFont = FontFamily(Font(Res.font.quran_juz))
    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AppTile(
            title = "Juz ${juz.number}",
            subtitle = "Starts at Surah ${juz.startsAt.surah} · Ayah ${juz.startsAt.ayah}",
            leading = { NumberBadge(juz.number) },
            trailing = { JuzName(juz.number, nameFont, expanded) { expanded = !expanded } },
            position = if (expanded) TilePosition.First else TilePosition.Single,
            onClick = onOpen,
        )
        if (expanded) juz.surahs.forEachIndexed { i, s ->
            SurahRow(s, if (i == juz.surahs.lastIndex) TilePosition.Last else TilePosition.Middle) { onOpenSurah(s) }
        }
    }
}

// ornate juz name + the expand chevron (its own tap target, so the tile tap can open instead)
@Composable
private fun JuzName(number: Int, nameFont: FontFamily, expanded: Boolean, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(number.toJuzKey(), fontFamily = nameFont, color = AppTheme.colors.primary, fontSize = 18.sp)
        Spacer(Modifier.size(6.dp))
        Icon(
            Lucide.ChevronDown, "Expand", tint = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onToggle).padding(4.dp).size(20.dp).rotate(if (expanded) 180f else 0f),
        )
    }
}
