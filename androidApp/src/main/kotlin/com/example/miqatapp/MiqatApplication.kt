package com.example.miqatapp

import android.app.Application
import com.example.miqatapp.core.di.initKoin
import com.example.miqatapp.core.focus.FocusScheduling
import com.example.miqatapp.core.focus.PhoneSilencer
import com.example.miqatapp.core.platform.AppCtx
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.notifications.scheduler.NotificationScheduler
import com.example.miqatapp.feature.widget.WidgetPublisher
import java.util.Locale

class MiqatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCtx.context = this // background code (the silence service) reads this
        initKoin()
        Locale.setDefault(Locale(SettingsStore.language.value.code)) // background alerts build strings off the UI; mirror the app language so they aren't in the system language
        PhoneSilencer.rescheduleAll() // arm today's remaining prayer windows on cold start
        FocusScheduling.start() // re-arm whenever times or focus settings change
        NotificationScheduler.start() // build prayer/dhikr/surah reminders + re-arm on any change
        WidgetPublisher.start() // publish today's times to the widget store + re-arm widget refreshes
    }
}
