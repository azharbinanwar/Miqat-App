package com.example.miqatapp.core.datetime

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** A date on the Islamic (Umm al-Qura) calendar. [month] is 1–12 (see [HijriMonth]). */
data class HijriDate(val day: Int, val month: Int, val year: Int)

/**
 * Convert a Gregorian date to the Umm al-Qura Hijri date using the platform's built-in Islamic calendar
 * (NSCalendar on iOS, android.icu on Android) — offline, no library. Both use Umm al-Qura so they agree.
 */
expect fun toHijri(date: LocalDate): HijriDate

/** Today's Hijri date shifted by [offsetDays] — the moon-sighting adjustment the user sets. */
fun hijriToday(offsetDays: Int = 0): HijriDate = toHijri(currentDate().plus(offsetDays, DateTimeUnit.DAY))
