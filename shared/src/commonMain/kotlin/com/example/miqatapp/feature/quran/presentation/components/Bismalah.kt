package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.quran.data.QuranStore
import com.example.miqatapp.feature.quran.data.QuranSymbols
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_juz
import org.jetbrains.compose.resources.Font

private const val BISMALAH_MAX_SP = 40f // above this the glyph would overflow, so it's capped

// stylish basmalah (juz font's decorative ﷽), shown at a surah start except Al-Fatiha and At-Tawbah
@Composable
fun Bismalah() {
    val fontSize by QuranStore.fontSize.collectAsState()
    Text(
        QuranSymbols.BASMALAH, Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 8.dp),
        fontFamily = FontFamily(Font(Res.font.quran_juz)), fontSize = (fontSize * 1.9f).coerceAtMost(BISMALAH_MAX_SP).sp,
        color = AppTheme.colors.primary, textAlign = TextAlign.Center, maxLines = 1,
    )
}
