package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.util.Calendar

actual fun currentDate(): LocalDate {
    val c = Calendar.getInstance()
    return LocalDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

actual fun currentTime(): LocalTime {
    val c = Calendar.getInstance()
    return LocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND))
}
