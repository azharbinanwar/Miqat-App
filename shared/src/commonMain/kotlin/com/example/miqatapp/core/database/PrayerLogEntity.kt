package com.example.miqatapp.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One prayer's status for a given day. */
@Entity(tableName = "prayer_log")
data class PrayerLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,      // ISO date, e.g. 2026-06-29
    val prayer: String,    // Fajr, Dhuhr, Asr, Maghrib, Isha
    val status: String,    // prayed / missed / jamaah
)
