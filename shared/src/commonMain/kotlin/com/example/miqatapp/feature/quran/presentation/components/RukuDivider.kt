package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme

// separator between rukus
@Composable
fun RukuDivider() {
    HorizontalDivider(Modifier.fillMaxWidth().padding(horizontal = 56.dp, vertical = 12.dp), color = AppTheme.colors.outlineVariant)
}
