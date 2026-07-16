package com.example.miqatapp

import android.app.Application
import com.example.miqatapp.core.di.initKoin
import com.example.miqatapp.core.focus.FocusScheduling
import com.example.miqatapp.core.focus.PhoneSilencer
import com.example.miqatapp.core.platform.AppCtx
import com.example.miqatapp.feature.notifications.scheduler.NotificationScheduler

class MiqatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCtx.context = this // background code (the silence service) reads this
        initKoin()
        PhoneSilencer.rescheduleAll() // arm today's remaining prayer windows on cold start
        FocusScheduling.start() // re-arm whenever times or focus settings change
        NotificationScheduler.start() // build prayer/dhikr/surah reminders + re-arm on any change
    }
}
