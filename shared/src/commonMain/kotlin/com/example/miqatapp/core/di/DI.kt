package com.example.miqatapp.core.di

import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * All DI modules. Feature modules (repositories, use cases, view models)
 * get added to [appModules] as they're built.
 */
val appModule = module {
    // feature registrations go here as they need DI — the pure engine and the stores don't
}

val appModules = listOf(appModule, databaseModule, platformDatabaseModule())

/** Start Koin once per platform entry point. */
fun initKoin() {
    startKoin {
        modules(appModules)
    }
}

/** Swift-friendly entry point — call from iOSApp.init(). */
fun startKoinForIos() = initKoin()
