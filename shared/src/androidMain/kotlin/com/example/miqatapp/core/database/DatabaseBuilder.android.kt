package com.example.miqatapp.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.miqatapp.core.constants.AppConst

/** Android builder — needs a Context (wire via DI later). */
fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(AppConst.DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(appContext, dbFile.absolutePath)
}
