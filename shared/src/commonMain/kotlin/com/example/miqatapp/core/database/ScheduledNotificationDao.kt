package com.example.miqatapp.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// Funcs are suspend (KMP). observeUpcoming feeds the upcoming screen.
@Dao
interface ScheduledNotificationDao {
    @Query("SELECT * FROM scheduled_notification ORDER BY fireAtMillis")
    fun observeUpcoming(): Flow<List<ScheduledNotificationEntity>>

    @Query("SELECT * FROM scheduled_notification ORDER BY fireAtMillis")
    suspend fun getAll(): List<ScheduledNotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<ScheduledNotificationEntity>)

    @Query("DELETE FROM scheduled_notification")
    suspend fun clear()

    // Swap the whole mirror in one transaction.
    @Transaction
    suspend fun replaceAll(rows: List<ScheduledNotificationEntity>) {
        clear()
        insertAll(rows)
    }
}
