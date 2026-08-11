package dev.tcode.thinmp.player

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
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * The controls stay live in two states where there is no player to drive: before start() has built
 * one, and after the queue emptied out and release() freed it. The first one crashed on the
 * lateinit - a config change destroys the activity, MainApplication stops the service, and the
 * retained view models rebind while isServiceRunning is still set, which creates a fresh service
 * that start() never ran on. The mini player is still on screen from the state it kept, so its play
 * button reaches a service holding no player at all.
 *
 * Nothing here plays audio, so no MediaStore content is needed and the test never skips itself.
 */
@RunWith(AndroidJUnit4::class)
class MusicServicePlayerGuardTest {
    private val timeoutMs = 15_000L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var connection: ServiceConnection
    private lateinit var service: MusicService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        // start() calls startForeground(), which the platform refuses while the app is in the
        // background, so the app has to actually be on screen for this to be testable at all.
        scenario = ActivityScenario.launch(MainActivity::class.java)
        service = bindService()
    }

    @After
    fun tearDown() {
        context.unbindService(connection)
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
    }

    /** A service that was bound into existence without start() ever running. */
    @Test
    fun controlsAreIgnoredBeforeTheFirstStart() {
        assertControlsAreIgnored()
    }

    /**
     * The only song fails, so retry() empties the list and returns after release(). getCurrentSong()
     * was already guarded and keeps the mini player from repainting, but the buttons it left on
     * screen still reach the service.
     */
    @Test
    fun controlsAreIgnoredAfterTheQueueEmpties() {
        val errored = CountDownLatch(1)

        service.addEventListener(object : MusicServiceListener {
            override fun onError() {
                errored.countDown()
            }
        })

        onMain { service.start(listOf(missingSong("999999998")), 0) }

        assertTrue("the missing song never raised an error", errored.await(timeoutMs, TimeUnit.MILLISECONDS))

        assertControlsAreIgnored()
    }

    /** Every entry point the player screen and the mini player can reach without a current song. */
    private fun assertControlsAreIgnored() {
        assertEquals("getCurrentPosition", 0L, onMain { service.getCurrentPosition() })
        assertNull("getCurrentSong", onMain { service.getCurrentSong() })
        assertDoesNotThrow("play") { service.play() }
        assertDoesNotThrow("pause") { service.pause() }
        assertDoesNotThrow("next") { service.next() }
        assertDoesNotThrow("prev") { service.prev() }
        assertDoesNotThrow("seekTo") { service.seekTo(1000) }
        assertDoesNotThrow("changeRepeat") { service.changeRepeat() }
        assertDoesNotThrow("changeShuffle") { service.changeShuffle() }
    }

    /**
     * Caught here only so the run reports which call failed; in the app nothing catches it and the
     * process goes down.
     */
    private fun assertDoesNotThrow(name: String, call: () -> Unit) {
        val thrown = onMain {
            try {
                call()
                null
            } catch (e: Throwable) {
                e
            }
        }

        assertNull("$name reached the player: $thrown", thrown)
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    /** ExoPlayer is built on the main looper, so everything that touches it has to go there. */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun bindService(): MusicService {
        val latch = CountDownLatch(1)
        var bound: MusicService? = null

        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                bound = (binder as MusicService.MusicBinder).getService()
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }

        context.bindService(Intent(context, MusicService::class.java), connection, Context.BIND_AUTO_CREATE)
        assertTrue("the service did not bind", latch.await(timeoutMs, TimeUnit.MILLISECONDS))

        return bound!!
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
