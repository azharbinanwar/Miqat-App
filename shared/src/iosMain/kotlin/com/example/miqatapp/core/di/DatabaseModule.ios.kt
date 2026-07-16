package com.example.miqatapp.core.di

import androidx.room.RoomDatabase
import com.example.miqatapp.core.database.AppDatabase
import com.example.miqatapp.core.database.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<AppDatabase>> { getDatabaseBuilder() }
}
