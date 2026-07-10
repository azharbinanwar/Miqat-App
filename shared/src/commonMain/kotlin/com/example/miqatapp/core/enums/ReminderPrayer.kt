package com.example.miqatapp.core.enums

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.prayer_asr
import com.example.miqatapp.resources.prayer_dhuhr
import com.example.miqatapp.resources.prayer_fajr
import com.example.miqatapp.resources.prayer_isha
import com.example.miqatapp.resources.prayer_jumuah
import com.example.miqatapp.resources.prayer_maghrib
import org.jetbrains.compose.resources.StringResource

/**
 * The prayers that carry reminders / Miqat-Focus config. The five daily prayers plus **Jumu'ah**, which
 * is the Friday variant of Dhuhr (same time slot — [time] = Dhuhr) and gets a longer [maxFocusMin] for the
 * khutbah. Icon comes from the time-calculation [Miqat] enum; [labelRes] carries the localized name.
 */
enum class ReminderPrayer(
    val time: Miqat,
    val labelRes: StringResource,
    val maxFocusMin: Int = 120,
    val fridayOnly: Boolean = false,
) {
    Fajr(Miqat.Fajr, Res.string.prayer_fajr),
    Dhuhr(Miqat.Dhuhr, Res.string.prayer_dhuhr),
    Asr(Miqat.Asr, Res.string.prayer_asr),
    Maghrib(Miqat.Maghrib, Res.string.prayer_maghrib),
    Isha(Miqat.Isha, Res.string.prayer_isha),
    Jumuah(Miqat.Dhuhr, Res.string.prayer_jumuah, maxFocusMin = 180, fridayOnly = true), // Friday Dhuhr — runs longer
    ;

    val icon: ImageVector get() = time.icon

    companion object {
        /** The everyday prayers (excludes the Friday-only Jumu'ah). */
        val daily get() = entries.filter { !it.fridayOnly }
    }
}
