package com.example.miqatapp.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Repeat
import com.example.miqatapp.config.theme.AppTheme

/** Soft accent pill showing a value + a swap hint — for rows that flip between two options on tap. */
@Composable
fun SwapPill(label: String) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.clip(shape).background(c.primary.copy(alpha = 0.12f))
            .border(1.dp, c.primary.copy(alpha = 0.40f), shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.primary)
        Icon(Lucide.Repeat, null, tint = c.primary.copy(alpha = 0.75f), modifier = Modifier.size(12.dp))
    }
}
