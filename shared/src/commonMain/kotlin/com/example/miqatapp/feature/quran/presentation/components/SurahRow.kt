package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.TilePosition
import com.example.miqatapp.feature.quran.data.Surah
import com.example.miqatapp.feature.quran.toSurahKey
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_surah_name
import org.jetbrains.compose.resources.Font

// one surah tile — pass position to place it in a group, or omit it for a standalone (single) tile
@Composable
fun SurahRow(surah: Surah, position: TilePosition = TilePosition.Single, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    val nameFont = FontFamily(Font(Res.font.quran_surah_name))
    AppTile(
        title = surah.nameTransliterated,
        subtitle = "${surah.nameEnglish} · ${surah.ayahCount} ayahs · ${surah.revelation.label}",
        leading = { NumberBadge(surah.number) },
        trailing = { Text(surah.number.toSurahKey(), fontFamily = nameFont, color = AppTheme.colors.primary, fontSize = 28.sp) },
        position = position,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
