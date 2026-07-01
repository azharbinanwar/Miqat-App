package com.example.miqatapp.core.datetime

import android.icu.util.GregorianCalendar
import android.icu.util.IslamicCalendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

actual fun toHijri(date: LocalDate): HijriDate {
    val islamic = IslamicCalendar().apply {
        calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
        // seed from the Gregorian date (month is 0-based in Calendar)
        timeInMillis = GregorianCalendar(date.year, date.month.number - 1, date.day).timeInMillis
    }
    return HijriDate(
        day = islamic.get(IslamicCalendar.DAY_OF_MONTH),
        month = islamic.get(IslamicCalendar.MONTH) + 1,
        year = islamic.get(IslamicCalendar.YEAR),
    )
}
