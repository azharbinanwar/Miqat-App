package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.quran.data.QuranSymbols
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_juz
import org.jetbrains.compose.resources.Font

// loading screen: juz font's decorative Quran Kareem glyph ("quran" ligature) + spinner
@Composable
fun QuranCalligraphy() {
    val colors = AppTheme.colors
    val juz = FontFamily(Font(Res.font.quran_juz))
    Box(Modifier.fillMaxSize().background(colors.background), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(QuranSymbols.QURAN, fontFamily = juz, fontSize = 120.sp, color = colors.primary)
            Spacer(Modifier.size(28.dp))
            CircularProgressIndicator(color = colors.primary)
        }
    }
}
