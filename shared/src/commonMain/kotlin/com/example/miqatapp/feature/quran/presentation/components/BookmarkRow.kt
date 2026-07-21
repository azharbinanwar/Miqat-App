package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.TilePosition

// a saved bookmark row
@Composable
fun BookmarkRow(surah: Int, ayah: Int, text: String, position: TilePosition, onClick: () -> Unit) {
    val colors = AppTheme.colors
    AppTile(
        title = "Surah $surah · Ayah $ayah",
        subtitle = text,
        leading = { Box(Modifier.size(38.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) { Icon(Lucide.Bookmark, null, tint = colors.primary, modifier = Modifier.size(20.dp)) } },
        position = position,
        onClick = onClick,
    )
}
