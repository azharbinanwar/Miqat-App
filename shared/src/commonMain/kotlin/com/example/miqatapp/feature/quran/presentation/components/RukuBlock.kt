package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.quran.data.AyahRef

// one ruku: surah header + basmalah at a surah start, the flowing ayahs, a ع marker at the end.
// A juz that begins on the first ayah shows before the surah header; one that begins mid-ruku splits the passage.
@Composable
fun RukuBlock(ruku: List<Ayah>, rukuNumber: Int, nextRukuNumber: Int?, prevJuz: Int, selected: AyahRef?, onSelect: (AyahRef) -> Unit) {
    val first = ruku.first()
    var prev = prevJuz
    // a juz that begins on this ruku's first ayah comes BEFORE the surah header (you enter the juz, then the surah)
    if (first.juz != prev) {
        JuzMarker(first.juz, modifier = Modifier.padding(vertical = 14.dp))
        prev = first.juz
    }
    if (first.ayah == 1) {
        SurahHeader(first.surah)
        // surah 1 include bismilah, 9 exclude as it's surah Tawbah
        if (first.surah != 1 && first.surah != 9) Bismalah()
    }
    // a juz that begins mid-ruku splits the passage exactly at its first ayah
    var runStart = 0
    for (k in 1 until ruku.size) {
        if (ruku[k].juz != prev) {
            AyahPassage(ruku.subList(runStart, k), selected, onSelect)
            JuzMarker(ruku[k].juz, modifier = Modifier.padding(vertical = 14.dp))
            runStart = k
            prev = ruku[k].juz
        }
    }
    AyahPassage(ruku.subList(runStart, ruku.size), selected, onSelect)
    RukuMarker(rukuNumber = rukuNumber, nextRukuNumber = nextRukuNumber)
}
