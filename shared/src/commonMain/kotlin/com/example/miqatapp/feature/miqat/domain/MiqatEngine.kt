package com.example.miqatapp.feature.miqat.domain

import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.constants.defaults.MiqatDefaults
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Miqat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan
import kotlin.time.Duration.Companion.minutes

/**
 * Pure sun-position engine. Give it a day + place + settings, get every Miqat point back — already
 * converted to our model and the place's wall-clock. Reads nothing, holds nothing (algorithm: PrayTimes).
 */
object MiqatEngine {

    fun timesFor(date: LocalDate, place: Place, calc: MiqatCalculation): List<MiqatTime> {
        val lat = place.latitude
        val lng = place.longitude
        // sample the sun at local time, not UTC — hence the longitude term
        val jDate = julian(date.year, date.monthNumber, date.dayOfMonth) - lng / (15 * 24)

        fun midDay(t: Double) = fixHour(12 - sunPosition(jDate + t).second)

        // hour angle for a twilight `angle` below the horizon; NaN when the sun never reaches it
        fun sunAngle(angle: Double, t: Double, beforeNoon: Boolean): Double {
            val decl = sunPosition(jDate + t).first
            val v = (-dSin(angle) - dSin(lat) * dSin(decl)) / (dCos(lat) * dCos(decl))
            val hours = dAcos(v) / 15
            return midDay(t) + if (beforeNoon) -hours else hours
        }

        fun asr(shadow: Double, t: Double): Double {
            val decl = sunPosition(jDate + t).first
            val angle = -arccot(shadow + dTan(abs(lat - decl)))
            return sunAngle(angle, t, beforeNoon = false)
        }

        val fajrAngle = if (calc.method == CalculationMethod.Custom) calc.fajrAngle.toDouble() else calc.method.fajrAngle
        val shadow = if (calc.madhab == Madhab.Hanafi) 2.0 else 1.0

        var fajr = sunAngle(fajrAngle, 5.0 / 24, beforeNoon = true)
        val sunrise = sunAngle(SUN_HORIZON, 6.0 / 24, beforeNoon = true)
        val dhuhr = midDay(12.0 / 24)
        val asr = asr(shadow, 13.0 / 24)
        val sunset = sunAngle(SUN_HORIZON, 18.0 / 24, beforeNoon = false)
        val interval = calc.method.ishaIntervalMinutes
        var isha = interval?.let { sunset + it / 60.0 }
            ?: sunAngle(if (calc.method == CalculationMethod.Custom) calc.ishaAngle.toDouble() else calc.method.ishaAngle!!, 18.0 / 24, beforeNoon = false)

        // high latitudes: the sun may never reach the twilight angle, leaving Fajr/Isha NaN or absurd
        val night = fixHour(sunrise - sunset)
        fun portion(angle: Double) = night * when (calc.highLatRule) {
            HighLatRule.MiddleNight -> 0.5
            HighLatRule.SeventhNight -> 1.0 / 7
            HighLatRule.AngleBased -> angle / 60.0
        }
        portion(fajrAngle).let { p -> if (fajr.isNaN() || fixHour(sunrise - fajr) > p) fajr = sunrise - p }
        if (interval == null) {
            val ishaAngle = if (calc.method == CalculationMethod.Custom) calc.ishaAngle.toDouble() else calc.method.ishaAngle!!
            portion(ishaAngle).let { p -> if (isha.isNaN() || fixHour(isha - sunset) > p) isha = sunset + p }
        }

        val shift = -lng / 15                                   // local-mean hours → UTC (timezone 0)
        val f = fajr + shift; val sr = sunrise + shift; val dh = dhuhr + shift
        val ar = asr + shift; val ss = sunset + shift; val ish = isha + shift
        val nightDur = (f + 24) - ss                            // ponytail: uses today's Fajr for next-Fajr (drifts <1min)

        val hours = mapOf(
            Miqat.Imsak to f - MiqatDefaults.IMSAK_OFFSET_MIN / 60.0,
            Miqat.Fajr to f,
            Miqat.Sunrise to sr,
            Miqat.Ishraq to sr + MiqatDefaults.ISHRAQ_OFFSET_MIN / 60.0,
            Miqat.Zawal to dh,                                  // istiwa — Dhuhr begins right after
            Miqat.Dhuhr to dh,
            Miqat.Asr to ar,
            Miqat.Sunset to ss,
            Miqat.Maghrib to ss,
            Miqat.Isha to ish,
            Miqat.Midnight to ss + nightDur / 2,
            Miqat.LastThird to ss + nightDur * 2 / 3,
        )

        val tz = TimeZone.of(place.timeZone)
        val dayStartUtc = date.atStartOfDayIn(TimeZone.UTC)
        return hours.map { (miqat, h) ->
            val totalMin = (h * 60).roundToInt() + (calc.adjustments[miqat] ?: 0)   // nearest-minute rounding
            MiqatTime(miqat, (dayStartUtc + totalMin.minutes).toLocalDateTime(tz))
        }.sortedBy { it.at }
    }

    private const val SUN_HORIZON = 0.833   // sunrise/sunset angle: refraction + sun radius

    // ── degree-based trig + the PrayTimes sun model ──

    private fun rad(d: Double) = d * PI / 180
    private fun deg(r: Double) = r * 180 / PI
    private fun dSin(d: Double) = sin(rad(d))
    private fun dCos(d: Double) = cos(rad(d))
    private fun dTan(d: Double) = tan(rad(d))
    private fun dAsin(x: Double) = deg(asin(x))
    private fun dAcos(x: Double) = deg(acos(x))
    private fun dAtan2(y: Double, x: Double) = deg(atan2(y, x))
    private fun arccot(x: Double) = deg(atan2(1.0, x))
    private fun fixAngle(a: Double) = ((a % 360) + 360) % 360
    private fun fixHour(h: Double) = ((h % 24) + 24) % 24

    private fun julian(y0: Int, m0: Int, day: Int): Double {
        var y = y0; var m = m0
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /** @return declination and equation-of-time (hours) for the given Julian day. */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dSin(g) + 0.020 * dSin(2 * g))
        val e = 23.439 - 0.00000036 * d
        val decl = dAsin(dSin(e) * dSin(l))
        val ra = fixHour(dAtan2(dCos(e) * dSin(l), dCos(l)) / 15)
        return decl to (q / 15 - ra)
    }
}
