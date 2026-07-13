package com.example.miqatapp.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CloudSun
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.SunMedium
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.ishraq
import com.example.miqatapp.resources.last_third_of_the_night
import com.example.miqatapp.resources.miqat_imsak
import com.example.miqatapp.resources.miqat_midnight
import com.example.miqatapp.resources.miqat_sunset
import com.example.miqatapp.resources.miqat_zawal
import com.example.miqatapp.resources.prayer_asr
import com.example.miqatapp.resources.prayer_dhuhr
import com.example.miqatapp.resources.prayer_fajr
import com.example.miqatapp.resources.prayer_isha
import com.example.miqatapp.resources.prayer_maghrib
import com.example.miqatapp.resources.prayer_sunrise
import com.example.miqatapp.resources.prayer_jumuah
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Every time point the app can show ("miqat" = appointed time). Replaces the old Prayer enum.
 * Declaration order is chronological through the day, so any filtered list is
 * already sorted for the UI.
 *
 * Not separate entries (aliases — same instant as an existing entry):
 *  - Iftar   = [Maghrib]
 *  - Tahajjud= [LastThird]
 */
enum class Miqat(
    val icon: ImageVector,
    val labelRes: StringResource,
    val category: Category,
) {
    Imsak(Lucide.Moon, Res.string.miqat_imsak, Category.RAMADAN),
    Fajr(Lucide.Sunrise, Res.string.prayer_fajr, Category.PRAYER),
    Sunrise(Lucide.SunMedium, Res.string.prayer_sunrise, Category.SOLAR),
    Ishraq(Lucide.SunMedium, Res.string.ishraq, Category.SOLAR),
    Zawal(Lucide.Sun, Res.string.miqat_zawal, Category.SOLAR),
    Dhuhr(Lucide.Sun, Res.string.prayer_dhuhr, Category.PRAYER),
    Asr(Lucide.CloudSun, Res.string.prayer_asr, Category.PRAYER),
    Sunset(Lucide.Sunset, Res.string.miqat_sunset, Category.SOLAR),
    Maghrib(Lucide.Sunset, Res.string.prayer_maghrib, Category.PRAYER),
    Isha(Lucide.Moon, Res.string.prayer_isha, Category.PRAYER),
    Midnight(Lucide.Moon, Res.string.miqat_midnight, Category.NIGHT),
    LastThird(Lucide.Moon, Res.string.last_third_of_the_night, Category.NIGHT);

    enum class Category { PRAYER, SOLAR, NIGHT, RAMADAN }

    val isPrayer: Boolean get() = category == Category.PRAYER

    /**
     * The obligatory prayer whose window contains this time; the UI nests it as a sub-row under that prayer.
     * Only Isha's window holds children (Midnight, LastThird). Imsak is a Ramadan dawn marker, not an Isha child.
     * Display grouping only; it never decides which prayer is active now.
     */
    val group: Miqat? get() = when (this) {
        Midnight, LastThird -> Isha
        else -> null
    }

    /** Localized display name. Pass the day for date rules (Friday Dhuhr reads as Jumu'ah); no date gives the plain name. */
    @Composable
    fun label(date: LocalDate? = null): String =
        if (this == Dhuhr && date?.dayOfWeek == DayOfWeek.FRIDAY) stringResource(Res.string.prayer_jumuah)
        else stringResource(labelRes)

    companion object {
        /** The five daily prayers (Tracker, notifications, logging). */
        val PRAYERS = entries.filter { it.category == Category.PRAYER }

        /** Jumu'ah is Friday Dhuhr. Its own label for config screens (focus, reminders) that list it separately. */
        @Composable
        fun jumuahLabel(): String = stringResource(Res.string.prayer_jumuah)
        /** The classic six-row list (5 prayers + Sunrise) that prayer-times screens traditionally show. */
        val DAILY = listOf(Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha)
        val SOLAR = entries.filter { it.category == Category.SOLAR }
        val NIGHT = entries.filter { it.category == Category.NIGHT }
        val RAMADAN = entries.filter { it.category == Category.RAMADAN }
    }
}

/** Theme color; new time points borrow the nearest prayer's color. */
val Miqat.color: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            Miqat.Imsak, Miqat.Fajr -> it.fajrColor
            Miqat.Sunrise, Miqat.Ishraq -> it.sunriseColor
            Miqat.Zawal, Miqat.Dhuhr -> it.dhuhrColor
            Miqat.Asr -> it.asrColor
            Miqat.Sunset, Miqat.Maghrib -> it.maghribColor
            Miqat.Isha, Miqat.Midnight, Miqat.LastThird -> it.ishaColor
        }
    }

val Miqat.onColor: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            Miqat.Imsak, Miqat.Fajr -> it.onFajrColor
            Miqat.Sunrise, Miqat.Ishraq -> it.onSunriseColor
            Miqat.Zawal, Miqat.Dhuhr -> it.onDhuhrColor
            Miqat.Asr -> it.onAsrColor
            Miqat.Sunset, Miqat.Maghrib -> it.onMaghribColor
            Miqat.Isha, Miqat.Midnight, Miqat.LastThird -> it.onIshaColor
        }
    }
