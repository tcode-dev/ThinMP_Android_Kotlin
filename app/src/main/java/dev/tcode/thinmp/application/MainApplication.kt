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
     * Leaving the app stops playback, as swiping it away does in MusicService.onTaskRemoved().
     * MainActivity has released PlaybackController's connection in onStop() by the time it is
     * destroyed, so nothing else is holding the service.
     *
     * A destroyed activity only means the app is going away when it is not coming straight back.
     * A configuration change - dark mode, font size, locale - destroys and recreates the activity,
     * and stopping the service there would end playback while the app is still on screen.
     * MainActivity declares configChanges="orientation|screenSize", so rotation never reaches here;
     * every other configuration change does.
     */
    override fun onActivityDestroyed(activity: Activity) {
        if (activity.isChangingConfigurations) return

        // Does nothing when the service is not running, so there is nothing to check first.
        val musicServiceIntent = Intent(applicationContext, MusicService::class.java)

        applicationContext.stopService(musicServiceIntent)
    }
}