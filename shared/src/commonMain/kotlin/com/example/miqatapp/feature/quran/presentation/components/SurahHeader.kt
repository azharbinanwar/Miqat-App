package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme

// ornate surah name at a surah start
@Composable
fun SurahHeader(surah: Int) {
    Text(
        surahLigature(surah), Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 6.dp),
        fontFamily = LocalQuranFonts.current.surah, fontSize = (LocalFontSize.current * 1.9f).sp,
        color = AppTheme.colors.primary, textAlign = TextAlign.Center, style = LIGATURES,
    )
}
