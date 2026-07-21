package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.quran.data.AyahRef
import com.example.miqatapp.feature.quran.data.QuranSymbols

// one ruku's ayahs, flowing together; tap an ayah to select it
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahPassage(ayahs: List<Ayah>, selected: AyahRef?, onSelect: (AyahRef) -> Unit) {
    val colors = AppTheme.colors
    FlowRow(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        ayahs.forEach { ayah ->
            val isSel = selected == ayah.ref
            ayahWords(ayah).forEach { w -> Word(w, isSel) { onSelect(ayah.ref) } }
            AyahEnd(ayah.ayah) { onSelect(ayah.ref) }
            if (ayah.endsRuku) Marker(QuranSymbols.RUKU, colors.onSurfaceVariant)
            if (ayah.sajda != null) Marker(QuranSymbols.SAJDA, colors.primary)
        }
    }
}

@Composable
private fun Word(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = text,
        fontFamily = LocalQuranFonts.current.body,
        fontSize = LocalFontSize.current.sp,
        textAlign = TextAlign.Center,
        color = colors.onBackground,
        maxLines = 1,
        softWrap = false,
        style = LIGATURES,
        modifier = Modifier
            .background(if (selected) colors.primary.copy(alpha = 0.14f) else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 2.dp),
    )
}

// number in ornate parentheses ﴾ ﴿, forced LTR so the surrounding RTL doesn't flip it
@Composable
private fun AyahEnd(number: Int, onClick: () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Text(
            text = QuranSymbols.AYAH_OPEN + arabicIndic(number) + QuranSymbols.AYAH_CLOSE,
            fontFamily = LocalQuranFonts.current.marker,
            fontSize = LocalFontSize.current.sp,
            color = AppTheme.colors.primary,
            style = LIGATURES,
            modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 3.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun Marker(glyph: String, color: Color) {
    Text(glyph, fontFamily = LocalQuranFonts.current.marker, fontSize = (LocalFontSize.current * 0.9f).sp, color = color, style = LIGATURES, modifier = Modifier.padding(horizontal = 2.dp))
}
