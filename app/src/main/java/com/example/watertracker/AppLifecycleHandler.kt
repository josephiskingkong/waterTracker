package com.example.watertracker

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log

class AppLifecycleHandler(private val context: Context) : Application.ActivityLifecycleCallbacks {

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        NotificationUtil.scheduleNotification(context)
    }
    private var isInForeground = false

    override fun onActivityPaused(activity: android.app.Activity) {
        if (isInForeground) {
            Log.d("AppLifecycleHandler", "App moved to background")
            handler.postDelayed(runnable, 5000)
            isInForeground = false
        }
    }

    override fun onActivityResumed(activity: android.app.Activity) {
        if (!isInForeground) {
            Log.d("AppLifecycleHandler", "App moved to foreground")
            handler.removeCallbacks(runnable)
            isInForeground = true
        }
    }

    override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: android.app.Activity) {}
    override fun onActivityStopped(activity: android.app.Activity) {}
    override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: android.app.Activity) {}
}
