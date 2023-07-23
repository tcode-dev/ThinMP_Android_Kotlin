package dev.tcode.thinmp.application

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import dagger.hilt.android.HiltAndroidApp
import dev.tcode.thinmp.player.MusicService

// 起動
//2023-07-22 19:03:39.415 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onCreate
//2023-07-22 19:03:39.433 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityCreated
//2023-07-22 19:03:39.453 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityStarted
//2023-07-22 19:03:39.454 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityResumed

// 回転
//2023-07-22 19:03:58.871 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityPaused
//2023-07-22 19:03:58.871 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MiniPlayerViewModel onStop
//2023-07-22 19:03:58.873 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityStopped
//2023-07-22 19:03:58.873 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivitySaveInstanceState
//2023-07-22 19:03:58.884 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityDestroyed
//2023-07-22 19:03:58.898 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityCreated
//2023-07-22 19:03:58.904 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityStarted
//2023-07-22 19:03:58.905 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityResumed

// app kill
//2023-07-22 19:04:42.678 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MiniPlayerViewModel onStop
//2023-07-22 19:04:42.690 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityStopped
//2023-07-22 19:04:42.691 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivitySaveInstanceState
//2023-07-22 19:04:42.768 17619-17619 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityDestroyed

// 画面ロック
//2023-07-22 19:09:47.092 18394-18394 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityPaused
//2023-07-22 19:09:47.134 18394-18394 System.out              dev.tcode.thinmp                     I  Log: MiniPlayerViewModel onStop
//2023-07-22 19:09:47.138 18394-18394 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivityStopped
//2023-07-22 19:09:47.139 18394-18394 System.out              dev.tcode.thinmp                     I  Log: MainApplication onActivitySaveInstanceState

@HiltAndroidApp
class MainApplication : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        println("Log: MainApplication onCreate")
        startService(Intent(applicationContext, DestroyingService::class.java))
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
        val destroyingServiceIntent = Intent(applicationContext, DestroyingService::class.java)
        val musicServiceIntent = Intent(applicationContext, MusicService::class.java)

        applicationContext.stopService(destroyingServiceIntent)
        applicationContext.stopService(musicServiceIntent)
    }

    class DestroyingService : Service() {

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }
    }
}