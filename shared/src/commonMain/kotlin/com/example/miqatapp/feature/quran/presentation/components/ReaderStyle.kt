package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.example.miqatapp.feature.quran.data.Ayah

// fonts + size provided once at the reader root, read by any component below
class QuranFonts(val body: FontFamily, val surah: FontFamily, val marker: FontFamily, val juz: FontFamily)

val LocalQuranFonts = staticCompositionLocalOf<QuranFonts> { error("QuranFonts not provided") }
val LocalFontSize = staticCompositionLocalOf { 15 }

// turns on Arabic ligatures/contextual shaping
val LIGATURES = TextStyle(fontFeatureSettings = "liga, calt")

private const val BASMALAH_WORDS = 4

// surah-name font ligates "surah001".."surah114" into the ornate name
fun surahLigature(surah: Int) = "surah" + surah.toString().padStart(3, '0')

// Latin → Arabic-Indic digits (٠١٢…)
fun arabicIndic(n: Int) = n.toString().map { (0x0660 + (it - '0')).toChar() }.joinToString("")

// split verses into rukus (a run ends where endsRuku is true)
fun groupByRuku(ayahs: List<Ayah>): List<List<Ayah>> {
    val out = ArrayList<List<Ayah>>()
    var run = ArrayList<Ayah>()
    for (a in ayahs) { run.add(a); if (a.endsRuku) { out.add(run); run = ArrayList() } }
    if (run.isNotEmpty()) out.add(run)
    return out
}

// display words of an ayah — the embedded basmalah is dropped on non-Fatiha surah starts (shown separately)
fun ayahWords(ayah: Ayah): List<String> {
    val words = ayah.text.split(' ').filter { it.isNotBlank() }
    return if (ayah.ayah == 1 && ayah.surah != 1 && words.size > BASMALAH_WORDS && words.first().startsWith("بِسْم"))
        words.drop(BASMALAH_WORDS) else words
}
