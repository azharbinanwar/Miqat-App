package com.example.miqatapp.feature.notifications.scheduler

import com.example.miqatapp.core.database.ScheduledNotificationEntity
import com.example.miqatapp.core.enums.NotificationType

// A concrete alert to fire: identity + when + how. Compute output, LocalNotifier input, mirrored to the DB.
data class NotificationEvent(
    val eventKey: String,
    val target: String,
    val kind: NotificationType,
    val fireAtMillis: Long,
    val slotId: Int = -1,
)

// Logical target keys for the non-prayer alerts (prayers use Miqat.key, Jumu'ah uses Miqat.jumuahKey).
object NotificationTarget {
    const val MULK = "mulk"
    const val KAHF = "kahf"
    const val MORNING = "morning_adhkar"
    const val EVENING = "evening_adhkar"
    const val TAHAJJUD = "tahajjud"
    const val ISHRAQ = "ishraq"
}

fun NotificationEvent.toEntity() = ScheduledNotificationEntity(
    slotId = slotId,
    eventKey = eventKey,
    target = target,
    kind = kind.name,
    fireAtMillis = fireAtMillis,
)
