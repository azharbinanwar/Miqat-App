package com.example.miqatapp.feature.quran.data

import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.tanzil_hafs
import com.example.miqatapp.resources.tanzil_me_quran
import com.example.miqatapp.resources.tanzil_naskh
import com.example.miqatapp.resources.tanzil_saleem
import com.example.miqatapp.resources.tanzil_scheherazade
import org.jetbrains.compose.resources.FontResource

/**
 * Pickable mushaf fonts, the genuine Tanzil set. They all render the same clean-Unicode Tanzil text,
 * so switching is just a font swap, no per-font db. Saleem is IndoPak-styled so it reads slightly
 * different on Uthmani spelling; kept as an option.
 */
enum class QuranFont(val label: String, val sample: String, val res: FontResource) {
    Hafs("Uthmani", "بِسْمِ اللَّهِ", Res.font.tanzil_hafs),
    Naskh("Naskh", "بِسْمِ اللَّهِ", Res.font.tanzil_naskh),
    MeQuran("me_quran", "بِسْمِ اللَّهِ", Res.font.tanzil_me_quran),
    Scheherazade("Scheherazade", "بِسْمِ اللَّهِ", Res.font.tanzil_scheherazade),
    Indopak("IndoPak", "بِسْمِ اللَّهِ", Res.font.tanzil_saleem);

    companion object { val DEFAULT = Hafs }
}
