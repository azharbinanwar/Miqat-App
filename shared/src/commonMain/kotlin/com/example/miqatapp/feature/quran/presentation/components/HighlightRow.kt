package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.TilePosition

// a saved highlight row — leading dot shows the highlight color
@Composable
fun HighlightRow(surah: Int, ayah: Int, text: String, color: Color, position: TilePosition, onClick: () -> Unit) {
    AppTile(
        title = "Surah $surah · Ayah $ayah",
        subtitle = text,
        leading = { Box(Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Box(Modifier.size(14.dp).clip(CircleShape).background(color)) } },
        position = position,
        onClick = onClick,
    )
}
