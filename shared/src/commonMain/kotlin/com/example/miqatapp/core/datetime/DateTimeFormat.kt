package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

/**
 * Flutter-style `DateFormat`: the pattern is the whole instruction, the method returns exactly what it
 * describes, with no fixed notion of "time" vs "date".
 *
 *   time.format("mm")               -> "08"                 (just the minute)
 *   time.format("HH:mm")            -> "17:08"              (time, 24h)
 *   time.format("h:mm a")           -> "5:08 PM"            (time, 12h)
 *   date.format("dd/MM/yyyy")       -> "12/07/2026"         (date)
 *   dt.format("yyyy-MM-dd HH:mm")   -> "2026-07-12 17:08"   (date + time)
 *
 * Unicode letters: y year, M month, d day, H 24h, h 12h, m minute, s second, a AM/PM.
 * byUnicodePattern rejects locale-dependent clock letters (h hh a K k), so we pre-expand just those from
 * the receiver's hour into quoted literals and hand the rest to it. Month/day NAMES (MMM, EEE) stay
 * unsupported — use a DSL format for those.
 */
@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDateTime.format(pattern: String): String =
    LocalDateTime.Format { byUnicodePattern(expandClock(pattern, hour)) }.format(this)

@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalTime.format(pattern: String): String =
    LocalTime.Format { byUnicodePattern(expandClock(pattern, hour)) }.format(this)

@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDate.format(pattern: String): String =
    LocalDate.Format { byUnicodePattern(pattern) }.format(this)

/** Rewrite 12h/AM-PM letters (h hh a K k) as quoted literals so byUnicodePattern accepts the pattern. */
private fun expandClock(pattern: String, hour: Int): String {
    if (pattern.none { it == 'h' || it == 'a' || it == 'K' || it == 'k' }) return pattern
    val out = StringBuilder()
    var i = 0
    var inQuote = false
    while (i < pattern.length) {
        val c = pattern[i]
        when {
            c == '\'' -> { inQuote = !inQuote; out.append(c); i++ }
            inQuote -> { out.append(c); i++ }
            c == 'h' -> {                                   // 12h clock: 0/12 -> 12
                var n = 0; while (i + n < pattern.length && pattern[i + n] == 'h') n++
                out.append('\'').append((((hour + 11) % 12) + 1).toString().padStart(if (n >= 2) 2 else 1, '0')).append('\'')
                i += n
            }
            c == 'a' -> { out.append(if (hour < 12) "'AM'" else "'PM'"); i++ }
            c == 'K' -> { out.append('\'').append(hour % 12).append('\''); i++ }        // 0-11
            c == 'k' -> { out.append('\'').append(if (hour == 0) 24 else hour).append('\''); i++ } // 1-24
            else -> { out.append(c); i++ }
        }
    }
    return out.toString()
}
