package dev.tcode.thinmp.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
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
 * The controls stay live in two states where there is nothing to play: before anything has given
 * the player a queue, and after retry() emptied it. A button left on screen can reach the service
 * in either state.
 *
 * The buttons reach the player through the session, so a crash would not be thrown back here:
 * it would take the process down, the test runner's included. Each test sends every control, gives
 * the session time to run them, and checks the service is still there to answer.
 *
 * Nothing here plays audio, so no MediaStore content is needed and the test never skips itself.
 */
@RunWith(AndroidJUnit4::class)
class MusicServicePlayerGuardTest {
    private val timeoutMs = 15_000L

    /** Long enough for the session to have run every command sent before it. */
    private val settleMs = 1_000L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: MediaController
    private val songRemoved = CountDownLatch(1)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        // Playback puts the service in the foreground, which the platform refuses while the app is
        // in the background, so the app has to actually be on screen for this to be testable at all.
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = connectController(context, timeoutMs, object : MediaController.Listener {
            override fun onCustomCommand(controller: MediaController, command: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
                if (command.customAction == MusicService.SONG_REMOVED.customAction) songRemoved.countDown()

                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        })
    }

    @After
    fun tearDown() {
        runOnMain { controller.release() }
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
    }

    /** A service that was brought up without anything ever being queued. */
    @Test
    fun controlsAreIgnoredBeforeTheFirstStart() {
        assertControlsAreIgnored()
    }

    /**
     * The only song fails, so retry() empties the queue and stops the player. The mini player hides
     * itself, but a button it left on screen can still reach the service.
     */
    @Test
    fun controlsAreIgnoredAfterTheQueueEmpties() {
        startOnController(controller, listOf(missingSong("999999998")), 0)

        assertTrue("the missing song was never dropped", songRemoved.await(timeoutMs, TimeUnit.MILLISECONDS))

        assertControlsAreIgnored()
    }

    /** Every control the player screen and the mini player can send without a current song. */
    private fun assertControlsAreIgnored() {
        runOnMain {
            controller.play()
            controller.pause()
            controller.seekToNext()
            controller.seekToNextMediaItem()
            controller.seekToPrevious()
            controller.seekToPreviousMediaItem()
            controller.seekTo(1000)
            controller.repeatMode = Player.REPEAT_MODE_ALL
            controller.shuffleModeEnabled = true
        }

        Thread.sleep(settleMs)

        assertTrue("the service went away", runOnMain { controller.isConnected })
        assertEquals("mediaItemCount", 0, runOnMain { controller.mediaItemCount })
        assertNull("currentMediaItem", runOnMain { controller.currentMediaItem })
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
