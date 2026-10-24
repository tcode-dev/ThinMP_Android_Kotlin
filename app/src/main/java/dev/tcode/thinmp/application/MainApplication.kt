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
     * アプリを離れたら再生を止める(MusicService.onTaskRemoved()のスワイプで消したときと同じ)。
     * MainActivityはonStop()でPlaybackControllerの接続を解放しているので、
     * ここでサービスを止めれば終わる。
     *
     * ダークモード、フォントサイズ、ロケールなどの構成変更でもActivityは破棄されるが、
     * すぐ作り直されるので止めない。
     * 回転はconfigChanges="orientation|screenSize"で扱うのでここには来ない。
     */
    override fun onActivityDestroyed(activity: Activity) {
        if (activity.isChangingConfigurations) return

        // サービスが動いていなくても問題ないので、事前に確かめない。
        val musicServiceIntent = Intent(applicationContext, MusicService::class.java)

        applicationContext.stopService(musicServiceIntent)
    }
}