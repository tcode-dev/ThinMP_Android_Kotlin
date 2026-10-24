package dev.tcode.thinmp.player

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.media3.session.MediaController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A song whose file has gone missing raises ERROR_CODE_IO_FILE_NOT_FOUND, and the service drops it
 * and starts again on the next one. That has to hold when the *first* song of the selection is the
 * missing one too, so playback never began before the error arrived.
 *
 * Both tests here end in "the real song is playing", which is the only observation that
 * distinguishes recovery from a service that stopped starting songs. Needs at least one audio
 * file on the device; skipped otherwise (tools/push-test-audio.sh).
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceRetryTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: MediaController
    private lateinit var realSong: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        val songs = runBlocking { SongRepository(context).findAll() }
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        realSong = songs.first()

        // Playback puts the service in the foreground, which the platform refuses while the app is
        // in the background, so the app has to actually be on screen for this to be testable at all.
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = connectController(context, timeoutMs)
    }

    @After
    fun tearDown() {
        runOnMain { controller.release() }
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
    }

    /** The failed song is dropped and the one after it plays. */
    @Test
    fun playsTheNextSongWhenTheFirstOneIsMissing() {
        startOnController(controller, listOf(missingSong("999999998"), realSong), 0)

        waitUntilPlaying()
        assertEquals(realSong.id, runOnMain { controller.currentMediaItem?.mediaId })
    }

    /** The retry chain has to keep going, not just survive one failure. */
    @Test
    fun playsTheFirstSongThatIsStillThere() {
        startOnController(controller, listOf(missingSong("999999998"), missingSong("999999997"), realSong), 0)

        waitUntilPlaying()
        assertEquals(realSong.id, runOnMain { controller.currentMediaItem?.mediaId })
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun waitUntilPlaying() {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            if (runOnMain { controller.isPlaying }) return

            Thread.sleep(pollMs)
        }

        fail("playback never started: the service is still holding the failed start")
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
