package com.example.miqatapp.core.datetime

import androidx.compose.runtime.Composable
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.dhu_al_hijjah
import com.example.miqatapp.resources.dhu_al_qadah
import com.example.miqatapp.resources.jumada_al_awwal
import com.example.miqatapp.resources.jumada_al_thani
import com.example.miqatapp.resources.muharram
import com.example.miqatapp.resources.rabi_al_awwal
import com.example.miqatapp.resources.rabi_al_thani
import com.example.miqatapp.resources.rajab
import com.example.miqatapp.resources.ramadan
import com.example.miqatapp.resources.safar
import com.example.miqatapp.resources.shaban
import com.example.miqatapp.resources.shawwal
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The twelve Hijri months, in order. Localized name carried on the enum (en/ar). */
enum class HijriMonth(val labelRes: StringResource) {
    Muharram(Res.string.muharram),
    Safar(Res.string.safar),
    RabiAlAwwal(Res.string.rabi_al_awwal),
    RabiAlThani(Res.string.rabi_al_thani),
    JumadaAlAwwal(Res.string.jumada_al_awwal),
    JumadaAlThani(Res.string.jumada_al_thani),
    Rajab(Res.string.rajab),
    Shaban(Res.string.shaban),
    Ramadan(Res.string.ramadan),
    Shawwal(Res.string.shawwal),
    DhuAlQadah(Res.string.dhu_al_qadah),
    DhuAlHijjah(Res.string.dhu_al_hijjah),
    ;

    @Composable
    fun label(): String = stringResource(labelRes)

    companion object {
        /** [number] is 1–12. */
        fun of(number: Int) = entries[number - 1]
    }
}
