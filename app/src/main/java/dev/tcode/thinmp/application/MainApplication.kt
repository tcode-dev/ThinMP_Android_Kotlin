package dev.tcode.thinmp.application

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import dagger.hilt.android.HiltAndroidApp
import dev.tcode.thinmp.player.MusicService

@HiltAndroidApp
class MainApplication : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        startService(Intent(this, DestroyingService::class.java))
        startService(Intent(this, MusicService::class.java))
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        println("Log: MainApplication: onActivityCreated")
    }

    override fun onActivityStarted(p0: Activity) {
        println("Log: MainApplication: onActivityStarted")
    }

    override fun onActivityResumed(p0: Activity) {
        println("Log: MainApplication: onActivityResumed")
    }

    override fun onActivityPaused(p0: Activity) {
        println("Log: MainApplication: onActivityPaused")
    }

    override fun onActivityStopped(p0: Activity) {
        println("Log: MainApplication: onActivityStopped")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        println("Log: MainApplication: onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(p0: Activity) {
        println("Log: MainApplication: onActivityDestroyed")
        val destroyingServiceIntent = Intent(applicationContext, DestroyingService::class.java)
        val musicServiceIntent = Intent(applicationContext, MusicService::class.java)

        applicationContext.stopService(destroyingServiceIntent)
        applicationContext.stopService(musicServiceIntent)
    }

    class DestroyingService : Service() {

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onDestroy() {
            super.onDestroy()

            println("Log: DestroyingService: onDestroy")
        }
    }
}