package com.example.miqatapp

import android.app.Application
import com.example.miqatapp.core.di.initKoin

class MiqatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
