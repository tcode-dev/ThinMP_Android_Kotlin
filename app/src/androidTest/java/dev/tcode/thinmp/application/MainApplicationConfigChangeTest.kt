package dev.tcode.thinmp.application

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.player.MusicService
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * MainApplication.onActivityDestroyed() stops the service when the activity goes away. A
 * configuration change - dark mode, font size, locale - destroys and recreates the activity, and
 * that used to reach the same stopService(): playback stopped while the app was still on screen.
 *
 * The service is started but not bound while the configuration change runs, which is the state the
 * app is in by the time the activity is destroyed - every view model unbinds in onStop(), before
 * onDestroy(). A binding held across the check would keep the service alive on its own and the
 * test would pass either way.
 *
 * What it compares is the service instance, not MusicService.isServiceRunning. The recreated
 * screen binds with BIND_AUTO_CREATE as soon as it sees the flag, so a service that was stopped is
 * replaced by a fresh one and the flag reads true again either way - only the player the old
 * instance held is gone.
 *
 * Nothing here plays audio, so no MediaStore content is needed and the test never skips itself.
 */
@RunWith(AndroidJUnit4::class)
class MainApplicationConfigChangeTest {
    private val timeoutMs = 15_000L

    private lateinit var context: Context
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        scenario = ActivityScenario.launch(MainActivity::class.java)
        context.startService(Intent(context, MusicService::class.java))
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        context.stopService(Intent(context, MusicService::class.java))
    }

    @Test
    fun theServiceSurvivesAConfigurationChange() {
        val before = currentService()

        scenario!!.recreate()

        assertSame("the configuration change replaced the service", before, currentService())
    }

    /** The other direction: leaving the app for real still has to stop it. */
    @Test
    fun theServiceStopsWhenTheActivityGoesAway() {
        currentService()

        scenario!!.close()
        scenario = null

        assertTrue("the service outlived the activity", awaitServiceStopped())
    }

    /**
     * Binds only long enough to read which instance is there, so nothing is holding the service
     * when the activity is destroyed.
     */
    private fun currentService(): MusicService {
        val latch = CountDownLatch(1)
        var bound: MusicService? = null
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                bound = (binder as MusicService.MusicBinder).getService()
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }

        context.bindService(Intent(context, MusicService::class.java), connection, Context.BIND_AUTO_CREATE)
        assertTrue("the service did not bind", latch.await(timeoutMs, TimeUnit.MILLISECONDS))
        context.unbindService(connection)

        return bound!!
    }

    /** stopService() is asynchronous, so onDestroy() lands after close() has returned. */
    private fun awaitServiceStopped(): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs

        while (System.currentTimeMillis() < deadline) {
            if (!MusicService.isServiceRunning) return true

            Thread.sleep(50)
        }

        return !MusicService.isServiceRunning
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
