package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Today's date from the platform calendar.
 * Avoids kotlinx-datetime's Clock/Instant, which differ across versions (0.6 vs 0.7)
 * and resolve differently per platform — the source of cross-platform build breakage.
 */
expect fun currentDate(): LocalDate

/** Device wall-clock time now (hour + minute), same platform-calendar approach. */
expect fun currentTime(): LocalTime
