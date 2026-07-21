package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontFamily
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.quran.data.QuranStore
import com.example.miqatapp.feature.quran.toSurahKey
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_surah_name
import org.jetbrains.compose.resources.Font

private const val SURAH_HEADER_MAX_SP = 60f // cap so the ornate name never overflows at large reading sizes

// ornate surah name at a surah start
@Composable
fun SurahHeader(surah: Int) {
    val fontSize by QuranStore.fontSize.collectAsState()
    Text(
        surah.toSurahKey(), Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 6.dp),
        fontFamily = FontFamily(Font(Res.font.quran_surah_name)), fontSize = (fontSize * 2.5f).coerceAtMost(SURAH_HEADER_MAX_SP).sp,
        color = AppTheme.colors.primary, textAlign = TextAlign.Center, maxLines = 1,
    )
}
