package com.example.watertracker

import android.app.Application

class WaterTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppLifecycleHandler(this))
    }
}
