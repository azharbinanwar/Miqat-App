package com.example.miqatapp.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.miqatapp.core.constants.AppConst

// exportSchema off while we use destructive migration in dev; turn on when real Migrations ship.
@Database(entities = [PrayerLogEntity::class, ScheduledNotificationEntity::class], version = AppConst.DB_VERSION, exportSchema = false)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerLogDao(): PrayerLogDao
    abstract fun scheduledNotificationDao(): ScheduledNotificationDao
}

/** The Room compiler (KSP) generates the actual implementation per platform. */
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Finalize a platform-provided builder into a database (defaults to Dispatchers.IO). */
fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        // ponytail: no persisted data shipped yet, so wipe on schema bump. Add real Migrations before that changes.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
