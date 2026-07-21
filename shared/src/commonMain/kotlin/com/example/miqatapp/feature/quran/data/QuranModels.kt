package com.example.miqatapp.feature.quran.data

// canonical keys — persist these, not the db id (which can shift)
data class AyahRef(val surah: Int, val ayah: Int) {
    fun toKey() = "$surah:$ayah"
    companion object { fun fromKey(k: String) = k.split(":").let { AyahRef(it[0].toInt(), it[1].toInt()) } }
}

data class WordRef(val surah: Int, val ayah: Int, val word: Int) {
    fun toKey() = "$surah:$ayah:$word"
    val ayahRef get() = AyahRef(surah, ayah)
    companion object { fun fromKey(k: String) = k.split(":").let { WordRef(it[0].toInt(), it[1].toInt(), it[2].toInt()) } }
}

// one `ayah` row
data class Ayah(
    val id: Int,
    val surah: Int,
    val ayah: Int,
    val text: String,
    val juz: Int,
    val endsRuku: Boolean,
    val sajda: Sajda?,
) {
    val ref get() = AyahRef(surah, ayah)
}

// one `surah` row
data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameTransliterated: String,
    val nameEnglish: String,
    val ayahCount: Int,
    val rukuCount: Int,
    val revelation: Revelation,
    val revelationOrder: Int,
    val startId: Int,
)
