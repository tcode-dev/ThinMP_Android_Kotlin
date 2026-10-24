package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.media3.session.MediaSessionService
import org.junit.Assert.assertTrue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * MusicServiceの破棄を検知する。
 * BIND_AUTO_CREATEなしでバインドするので、サービスを生かし続けず、破棄されると切断される。
 *
 * サービスが動いている間にattachすること。
 * 動いていないとバインドは起動待ちになり、「まだ接続していない」と「もう破棄された」を区別できない。
 */
class MusicServiceWatcher private constructor(private val context: Context) {
    private val connected = CountDownLatch(1)
    private val destroyed = CountDownLatch(1)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            connected.countDown()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            destroyed.countDown()
        }

        override fun onBindingDied(name: ComponentName) {
            destroyed.countDown()
        }
    }

    /** 結果にかかわらずバインドを解除するので、1つのウォッチャーは1回しか使えない。 */
    fun awaitDestroyed(timeoutMs: Long): Boolean {
        try {
            return destroyed.await(timeoutMs, TimeUnit.MILLISECONDS)
        } finally {
            context.unbindService(connection)
        }
    }

    companion object {
        /**
         * MediaControllerと同じアクションでバインドする。
         * これならMusicServiceがbinderを返すので、サービスがある間だけ続く接続になる。
         * 素のIntentではbinderが返らず、接続の通知が来ない。
         */
        private fun serviceIntent(context: Context): Intent {
            return Intent(MediaSessionService.SERVICE_INTERFACE).setComponent(ComponentName(context, MusicService::class.java))
        }

        fun attach(context: Context, timeoutMs: Long): MusicServiceWatcher {
            val watcher = MusicServiceWatcher(context)

            context.bindService(serviceIntent(context), watcher.connection, 0)
            assertTrue("the service is not running", watcher.connected.await(timeoutMs, TimeUnit.MILLISECONDS))

            return watcher
        }

        /**
         * サービスを止め、破棄されるまで待つ。
         * 先にBIND_AUTO_CREATEでバインドして必ず起動させるので、
         * 動いていてもいなくても同じように終わる。
         */
        fun stopAndAwait(context: Context, timeoutMs: Long) {
            val created = CountDownLatch(1)
            val creator = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    created.countDown()
                }

                override fun onServiceDisconnected(name: ComponentName) {}
            }

            context.bindService(serviceIntent(context), creator, Context.BIND_AUTO_CREATE)
            assertTrue("the service did not bind", created.await(timeoutMs, TimeUnit.MILLISECONDS))

            val watcher = attach(context, timeoutMs)

            context.unbindService(creator)
            context.stopService(Intent(context, MusicService::class.java))
            assertTrue("the service is still running", watcher.awaitDestroyed(timeoutMs))
        }
    }
}
