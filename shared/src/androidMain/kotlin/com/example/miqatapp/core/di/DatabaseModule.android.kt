package com.example.miqatapp.core.di

import androidx.room.RoomDatabase
import com.example.miqatapp.core.database.AppDatabase
import com.example.miqatapp.core.database.getDatabaseBuilder
import com.example.miqatapp.core.platform.AppCtx
import org.koin.core.module.Module
import org.koin.dsl.module

// AppCtx.context is set in MiqatApplication before the DB is ever touched (singles are lazy).
actual fun platformDatabaseModule(): Module = module {
    single<RoomDatabase.Builder<AppDatabase>> { getDatabaseBuilder(AppCtx.context) }
}
