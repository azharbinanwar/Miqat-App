package com.example.miqatapp.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MoonStar
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppCard
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.recite_before_sleep_after_isha
import com.example.miqatapp.resources.surah_al_mulk
import com.example.miqatapp.resources.verse_of_the_day
import org.jetbrains.compose.resources.stringResource

/** Nightly Surah Al-Mulk reminder tile. */
@Composable
fun MulkReminderCard() {
    AppTile(
        title = stringResource(Res.string.surah_al_mulk),
        subtitle = stringResource(Res.string.recite_before_sleep_after_isha),
        leadingIcon = Lucide.MoonStar,
        leadingColor = AppTheme.colors.primary,
        trailing = { Box(Modifier.size(8.dp).clip(CircleShape).background(AppTheme.colors.success)) },
    )
}

/** Verse of the day card. */
@Composable
fun DailyVerseCard() {
    AppCard(padding = 18.dp, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Lucide.BookOpen, null, tint = AppTheme.colors.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.verse_of_the_day), color = AppTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("\"Indeed, prayer prohibits immorality and wrongdoing.\"", color = AppTheme.colors.onSurface, fontSize = 15.sp)
        Text("Surah Al-'Ankabut 29:45", color = AppTheme.colors.onSurfaceVariant, fontSize = 12.sp)
    }
}
