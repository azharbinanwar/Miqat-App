package com.example.miqatapp.feature.qibla.domain

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Kaaba coordinates.
private const val KAABA_LAT = 21.4225
private const val KAABA_LNG = 39.8262
private const val EARTH_RADIUS_KM = 6371.0

private fun Double.toRad() = this * PI / 180.0
private fun Double.toDeg() = this * 180.0 / PI

/** Initial great-circle bearing (0..360, clockwise from true north) from a point to the Kaaba. */
fun qiblaBearing(lat: Double, lng: Double): Double {
    val phi1 = lat.toRad()
    val phi2 = KAABA_LAT.toRad()
    val dLng = (KAABA_LNG - lng).toRad()
    val y = sin(dLng)
    val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(dLng)
    return (atan2(y, x).toDeg() + 360.0) % 360.0
}

/** Great-circle distance in km from a point to the Kaaba (haversine). */
fun distanceToMakkahKm(lat: Double, lng: Double): Double {
    val phi1 = lat.toRad()
    val phi2 = KAABA_LAT.toRad()
    val dPhi = (KAABA_LAT - lat).toRad()
    val dLng = (KAABA_LNG - lng).toRad()
    val a = sin(dPhi / 2) * sin(dPhi / 2) + cos(phi1) * cos(phi2) * sin(dLng / 2) * sin(dLng / 2)
    return EARTH_RADIUS_KM * 2 * atan2(sqrt(a), sqrt(1 - a))
}

// kotlin.math has no tan; derive it.
private fun tan(x: Double) = sin(x) / cos(x)
