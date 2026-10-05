package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.junit.Assert.assertTrue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Tells when the running MusicService is destroyed, the way the app itself finds out: a binding
 * without BIND_AUTO_CREATE, which does not keep the service alive and dies along with it.
 *
 * It has to be attached while the service is running. Before then the binding would only wait for
 * one to start, and "not connected yet" and "already gone" look the same.
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

    /** Unbinds whatever the outcome, so the watcher is used up either way. */
    fun awaitDestroyed(timeoutMs: Long): Boolean {
        try {
            return destroyed.await(timeoutMs, TimeUnit.MILLISECONDS)
        } finally {
            context.unbindService(connection)
        }
    }

    companion object {
        fun attach(context: Context, timeoutMs: Long): MusicServiceWatcher {
            val watcher = MusicServiceWatcher(context)

            context.bindService(MusicService.bindIntent(context), watcher.connection, 0)
            assertTrue("the service is not running", watcher.connected.await(timeoutMs, TimeUnit.MILLISECONDS))

            return watcher
        }

        /**
         * Stops the service and returns once it is gone, whether or not it was running. A bind with
         * BIND_AUTO_CREATE first makes sure there is one to watch, so both cases end the same way.
         */
        fun stopAndAwait(context: Context, timeoutMs: Long) {
            val created = CountDownLatch(1)
            val creator = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    created.countDown()
                }

                override fun onServiceDisconnected(name: ComponentName) {}
            }

            context.bindService(MusicService.bindIntent(context), creator, Context.BIND_AUTO_CREATE)
            assertTrue("the service did not bind", created.await(timeoutMs, TimeUnit.MILLISECONDS))

            val watcher = attach(context, timeoutMs)

            context.unbindService(creator)
            context.stopService(Intent(context, MusicService::class.java))
            assertTrue("the service is still running", watcher.awaitDestroyed(timeoutMs))
        }
    }
}
