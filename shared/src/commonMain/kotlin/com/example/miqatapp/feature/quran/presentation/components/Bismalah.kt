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
import com.example.miqatapp.feature.quran.data.QuranSymbols

// stylish basmalah (juz font's decorative ﷽), shown at a surah start except Al-Fatiha and At-Tawbah
@Composable
fun Bismalah() {
    Text(
        QuranSymbols.BASMALAH, Modifier.fillMaxWidth().padding(vertical = 8.dp),
        fontFamily = LocalQuranFonts.current.juz, fontSize = (LocalFontSize.current * 1.9f).sp,
        color = AppTheme.colors.primary, textAlign = TextAlign.Center, style = LIGATURES,
    )
}
