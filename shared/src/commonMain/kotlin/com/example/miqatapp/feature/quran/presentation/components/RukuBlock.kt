package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.runtime.Composable
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.quran.data.AyahRef

// one ruku: surah header + basmalah at a surah start, the flowing ayahs, then a divider unless the surah ends here
@Composable
fun RukuBlock(ruku: List<Ayah>, divider: Boolean, selected: AyahRef?, onSelect: (AyahRef) -> Unit) {
    val first = ruku.first()
    if (first.ayah == 1) {
        SurahHeader(first.surah)
        // surah 1 include bismilah, 9 exclude as it's surah Tubha
        if (first.surah != 1 && first.surah != 9) Bismalah()
    }
    AyahPassage(ruku, selected, onSelect)
    if (divider) RukuDivider()
}
