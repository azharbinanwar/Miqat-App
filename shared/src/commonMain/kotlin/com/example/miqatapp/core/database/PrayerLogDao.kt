package com.example.miqatapp.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** DAO funcs are suspend (KMP requirement); Flow queries observe changes. */
@Dao
interface PrayerLogDao {
    @Insert
    suspend fun insert(entry: PrayerLogEntity)

    @Query("SELECT * FROM prayer_log WHERE date = :date")
    fun observeByDate(date: String): Flow<List<PrayerLogEntity>>
}
