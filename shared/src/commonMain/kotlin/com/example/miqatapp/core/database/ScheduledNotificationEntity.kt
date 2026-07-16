package com.example.miqatapp.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// One scheduled alert (one slot). Mirror of what the OS holds; the rebuild rewrites the whole set.
@Entity(
    tableName = "scheduled_notification",
    indices = [Index("fireAtMillis"), Index(value = ["eventKey"], unique = true)],
)
data class ScheduledNotificationEntity(
    @PrimaryKey val slotId: Int,     // 0..63 — the OS id (Android request code / iOS identifier)
    val eventKey: String,            // stable logical id, e.g. "fajr:AT:2026-07-16"
    val target: String,              // Miqat.key | Miqat.jumuahKey | mulk|kahf|morning_adhkar|evening_adhkar|tahajjud|ishraq
    val kind: String,                // NotificationType name: AT_TIME|REMIND_BEFORE|JAMAAT|REMINDER
    val fireAtMillis: Long,          // epoch millis when it fires
)
