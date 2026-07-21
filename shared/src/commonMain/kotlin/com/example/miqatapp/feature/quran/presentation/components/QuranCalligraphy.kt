package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.quran.data.QuranSymbols
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_juz
import org.jetbrains.compose.resources.Font

// the juz font's decorative Quran Kareem glyph ("quran" ligature), centered — shown while the reader loads
@Composable
fun QuranCalligraphy() {
    val colors = AppTheme.colors
    val juz = FontFamily(Font(Res.font.quran_juz))
    Box(Modifier.fillMaxSize().background(colors.background), Alignment.Center) {
        Text(QuranSymbols.QURAN, fontFamily = juz, fontSize = 120.sp, color = colors.primary, modifier = Modifier.offset(x = (-20).dp, y = 0.dp))
    }
}
