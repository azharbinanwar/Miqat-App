package com.example.miqatapp.feature.quran

// number → font-ligature keys (the surah/juz fonts render these as the ornate name glyph) + Arabic-Indic digits
fun Int.toSurahKey(): String = "surah" + toString().padStart(3, '0')   // 2 -> "surah002"
fun Int.toJuzKey(): String = "j" + toString().padStart(3, '0')       // 5 -> "j005"
fun Int.toArabicIndic(): String = toString().map { (0x0660 + (it - '0')).toChar() }.joinToString("") // 255 -> "٢٥٥"
