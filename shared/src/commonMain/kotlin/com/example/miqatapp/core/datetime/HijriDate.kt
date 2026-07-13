package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate

/** A date on the Islamic (Umm al-Qura) calendar. [month] is 1–12 (see [HijriMonth]). */
data class HijriDate(val day: Int, val month: Int, val year: Int)

/**
 * Convert a Gregorian date to the Umm al-Qura Hijri date using the platform's built-in Islamic calendar
 * (NSCalendar on iOS, android.icu on Android), offline, no library. Both use Umm al-Qura so they agree.
 * For "today's Hijri date" use [Now.hijri]; this is the raw converter for any date.
 */
expect fun toHijri(date: LocalDate): HijriDate
