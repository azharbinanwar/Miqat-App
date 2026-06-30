package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate
import java.util.Calendar

actual fun currentDate(): LocalDate {
    val c = Calendar.getInstance()
    return LocalDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}
