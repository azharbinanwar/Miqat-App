package com.example.miqatapp.core.di

import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * All DI modules. Feature modules (repositories, use cases, view models)
 * get added to [appModules] as they're built.
 */
val appModule = module {
    // registrations go here, e.g.:
    // single<MiqatRepository> { MiqatRepositoryImpl(get()) }
    // viewModel { PrayerViewModel(get()) }
}

val appModules = listOf(appModule)

/** Start Koin once per platform entry point. */
fun initKoin() {
    startKoin {
        modules(appModules)
    }
}

/** Swift-friendly entry point — call from iOSApp.init(). */
fun startKoinForIos() = initKoin()
