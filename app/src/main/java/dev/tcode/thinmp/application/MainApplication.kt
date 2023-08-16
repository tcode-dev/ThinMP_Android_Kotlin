package dev.tcode.thinmp.application

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.HiltAndroidApp
import dev.tcode.thinmp.player.MusicService

@HiltAndroidApp
class MainApplication : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityStarted(p0: Activity) {}

    override fun onActivityResumed(p0: Activity) {}

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(p0: Activity) {
        println("Log: MainApplication onActivityDestroyed")
        if (!MusicService.isServiceRunning) return

        val musicServiceIntent = Intent(applicationContext, MusicService::class.java)

        applicationContext.stopService(musicServiceIntent)
    }
}