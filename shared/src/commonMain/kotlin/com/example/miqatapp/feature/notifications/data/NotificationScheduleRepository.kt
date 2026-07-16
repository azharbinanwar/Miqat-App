package com.example.miqatapp.feature.notifications.data

import com.example.miqatapp.core.database.ScheduledNotificationDao
import com.example.miqatapp.core.database.ScheduledNotificationEntity
import kotlinx.coroutines.flow.Flow

// The scheduled-alert mirror. Scheduler writes after each rebuild; UI observes. Not the source of truth.
class NotificationScheduleRepository(private val dao: ScheduledNotificationDao) {
    fun observeUpcoming(): Flow<List<ScheduledNotificationEntity>> = dao.observeUpcoming()
    suspend fun getAll(): List<ScheduledNotificationEntity> = dao.getAll()
    suspend fun replaceAll(rows: List<ScheduledNotificationEntity>) = dao.replaceAll(rows)
    suspend fun clear() = dao.clear()
}
