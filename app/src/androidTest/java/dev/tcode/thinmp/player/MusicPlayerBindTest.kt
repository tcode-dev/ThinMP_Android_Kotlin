package dev.tcode.thinmp.player

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * There is no flag saying whether MusicService runs. A screen binds without BIND_AUTO_CREATE and
 * is connected exactly while it does, and start() brings the service up and has it start itself.
 *
 * Nothing here plays audio, so no MediaStore content is needed and the test never skips itself.
 */
@RunWith(AndroidJUnit4::class)
class MusicPlayerBindTest {
    private val timeoutMs = 15_000L

    /** How long a connection that must not happen is given to happen anyway. */
    private val quietMs = 1_000L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val bound = CountDownLatch(1)
    private val disconnected = CountDownLatch(1)
    private val musicPlayer = MusicPlayer(object : MusicPlayerListener {
        override fun onBind() {
            bound.countDown()
        }

        override fun onDisconnect() {
            disconnected.countDown()
        }
    })

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        // Stopped before the activity is up, since its own screens bind too. Starting a service,
        // and startForeground(), are refused while the app is in the background.
        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        onMain { musicPlayer.destroy(context) }
        scenario.close()
        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
    }

    @Test
    fun aScreenConnectsOnlyWhileTheServiceRuns() {
        onMain { musicPlayer.bindService(context) }

        assertFalse("binding brought up a service of its own", bound.await(quietMs, TimeUnit.MILLISECONDS))

        context.startService(Intent(context, MusicService::class.java))

        assertTrue("the waiting binding did not connect once the service started", bound.await(timeoutMs, TimeUnit.MILLISECONDS))

        context.stopService(Intent(context, MusicService::class.java))

        assertTrue("the service going away was not reported", disconnected.await(timeoutMs, TimeUnit.MILLISECONDS))
    }

    /**
     * start() creates the service by binding to it, and a service that is only bound is destroyed
     * with its last binding. Playback has to outlive the screen that started it.
     */
    @Test
    fun theServiceOutlivesTheScreenThatStartedIt() {
        onMain { musicPlayer.start(context, listOf(missingSong("999999997")), 0) }

        assertTrue("start() never connected", bound.await(timeoutMs, TimeUnit.MILLISECONDS))

        val watcher = MusicServiceWatcher.attach(context, timeoutMs)

        onMain { musicPlayer.destroy(context) }

        assertFalse("the service went away with the screen", watcher.awaitDestroyed(quietMs))
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    /** MusicPlayer is only ever driven from the main thread, where its callbacks arrive too. */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
