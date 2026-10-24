package dev.tcode.thinmp.application

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.MusicServiceWatcher
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MainApplication.onActivityDestroyed() stops the service when the activity goes away. A
 * configuration change - dark mode, font size, locale - destroys and recreates the activity, and
 * must not reach that stopService(): playback would stop while the app is still on screen.
 *
 * The service is started but not bound while the configuration change runs, which is the state the
 * app is in by the time the activity is destroyed - PlaybackController's connection is released in
 * onStop(), before onDestroy(). A binding held across the check would keep the service alive on its
 * own and the test would pass either way.
 *
 * What it watches is the instance going away, not whether some instance is running afterwards. The
 * recreated activity connects again with BIND_AUTO_CREATE, so a service that was stopped is
 * replaced by a fresh one and looks running either way - only the queue the old instance held is
 * gone. The watcher's own binding does not keep the service alive.
 *
 * Nothing here plays audio, so no MediaStore content is needed and the test never skips itself.
 */
@RunWith(AndroidJUnit4::class)
class MainApplicationConfigChangeTest {
    private val timeoutMs = 15_000L

    /** How long a destruction that must not happen is given to happen anyway. */
    private val quietMs = 1_000L

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
        val watcher = MusicServiceWatcher.attach(context, timeoutMs)

        scenario!!.recreate()

        assertFalse("the configuration change stopped the service", watcher.awaitDestroyed(quietMs))
    }

    /** The other direction: leaving the app for real still has to stop it. */
    @Test
    fun theServiceStopsWhenTheActivityGoesAway() {
        val watcher = MusicServiceWatcher.attach(context, timeoutMs)

        scenario!!.close()
        scenario = null

        // stopService() is asynchronous, so onDestroy() lands after close() has returned.
        assertTrue("the service outlived the activity", watcher.awaitDestroyed(timeoutMs))
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
