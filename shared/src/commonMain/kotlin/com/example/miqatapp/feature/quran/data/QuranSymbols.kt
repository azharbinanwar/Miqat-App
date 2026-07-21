package com.example.miqatapp.feature.quran.data

/** Quran display glyphs, one place so they're never sprinkled as literals across the UI. */
object QuranSymbols {
    const val AYAH_OPEN = "﴾"  // ornate parenthesis around the ayah number
    const val AYAH_CLOSE = "﴿"
    const val RUKU = "ع"        // section (ruku) marker
    const val SAJDA = "۩"       // prostration marker
    const val BASMALAH = "﷽"    // ligature bismillah
    const val QURAN = "quran" // this will be used ot show a glyph under juz font

    // bidi isolates: wrap the ayah number so its ornate parens don't mirror inside RTL text
    val LTR_ISOLATE = Char(0x2066).toString() // U+2066 LEFT-TO-RIGHT ISOLATE
    val POP_ISOLATE = Char(0x2069).toString() // U+2069 POP DIRECTIONAL ISOLATE
    val WORD_JOINER = Char(0x2060).toString() // U+2060 no line break here

    // ornate ayah number as one unbreakable unit, e.g. ﴾١٢﴿ that never splits across a line
    fun ayahNumber(arabicIndic: String) =
        LTR_ISOLATE + AYAH_OPEN + WORD_JOINER + arabicIndic + WORD_JOINER + AYAH_CLOSE + POP_ISOLATE
}
