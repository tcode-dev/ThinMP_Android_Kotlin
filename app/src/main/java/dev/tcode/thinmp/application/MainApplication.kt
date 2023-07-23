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

        println("Log: MainApplication onCreate")
        startService(Intent(applicationContext, MusicService::class.java))
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        println("Log: MainApplication onActivityCreated")
    }

    override fun onActivityStarted(p0: Activity) {
        println("Log: MainApplication onActivityStarted")
    }

    override fun onActivityResumed(p0: Activity) {
        println("Log: MainApplication onActivityResumed")
    }

    override fun onActivityPaused(p0: Activity) {
        println("Log: MainApplication onActivityPaused")
    }

    override fun onActivityStopped(p0: Activity) {
        println("Log: MainApplication onActivityStopped")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        println("Log: MainApplication onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(p0: Activity) {
        println("Log: MainApplication onActivityDestroyed")

        val musicServiceIntent = Intent(applicationContext, MusicService::class.java)

        applicationContext.stopService(musicServiceIntent)
    }
}