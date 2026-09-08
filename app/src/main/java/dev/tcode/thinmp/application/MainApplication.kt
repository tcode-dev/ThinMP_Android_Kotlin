package dev.tcode.thinmp.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.HiltAndroidApp
import dev.tcode.thinmp.player.MusicService

@HiltAndroidApp
class MainApplication : Application(), Application.ActivityLifecycleCallbacks {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityStarted(p0: Activity) {}

    override fun onActivityResumed(p0: Activity) {}

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    /**
     * A destroyed activity only means the app is going away when it is not coming straight back.
     * A configuration change - dark mode, font size, locale - destroys and recreates the activity,
     * and stopping the service there ended playback while the app was still on screen. The mini
     * player kept the state its retained view model held, so nothing on screen said playback had
     * stopped, and the recreated screen bound with BIND_AUTO_CREATE into a fresh MusicService that
     * start() had never run on. MainActivity declares configChanges="orientation|screenSize", so
     * rotation never reached here; every other configuration change did.
     */
    override fun onActivityDestroyed(activity: Activity) {
        if (activity.isChangingConfigurations) return

        if (!MusicService.isServiceRunning) return

        val musicServiceIntent = Intent(applicationContext, MusicService::class.java)

        applicationContext.stopService(musicServiceIntent)
    }
}