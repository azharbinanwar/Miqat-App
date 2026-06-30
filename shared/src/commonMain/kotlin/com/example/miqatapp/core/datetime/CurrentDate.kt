package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate

/**
 * Today's date from the platform calendar.
 * Avoids kotlinx-datetime's Clock/Instant, which differ across versions (0.6 vs 0.7)
 * and resolve differently per platform — the source of cross-platform build breakage.
 */
expect fun currentDate(): LocalDate
