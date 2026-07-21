package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme

// small circular index badge used as the leading of surah/juz rows
@Composable
fun NumberBadge(n: Int) {
    val colors = AppTheme.colors
    Box(Modifier.size(36.dp).clip(CircleShape).background(colors.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        Text("$n", color = colors.primary, fontSize = 13.sp)
    }
}
