package dev.tcode.thinmp.view.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.SystemClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.MusicServiceListener
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * When the song that failed was the only one left, retry() releases the player and returns, and the
 * player screen is then sitting on top of a service with nothing to drive: every button still
 * reaches it and media3 ignores all of it without so much as an exception. The mini player hides
 * itself in that state, so the two used to disagree about whether there is anything playing.
 *
 * Both tests start on a real song first: the screen registers its listener from a bind that
 * completes on its own schedule, and a title on screen is the only proof from out here that the
 * listener is in place before the failing start goes in. That needs an audio file on the device;
 * skipped otherwise (tools/push-test-audio.sh).
 */
@RunWith(AndroidJUnit4::class)
class PlayerScreenQueueEmptyTest {
    private val timeoutMs = 15_000L
    private val settleMs = 3_000L
    private val pollMs = 50L
    private val missingName = "missing"

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navigator = RecordingNavigator()

    private lateinit var context: Context
    private lateinit var connection: ServiceConnection
    private lateinit var service: MusicService
    private lateinit var realSong: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        // The screen is watched by its title, so the song has to be tellable apart from the one
        // that fails.
        val song = runBlocking { SongRepository(context).findAll() }.firstOrNull { it.name != missingName }
        assumeTrue("needs at least one audio file in MediaStore", song != null)
        realSong = song!!

        // The screen only binds while the service is already running, so it has to exist before the
        // composition starts. Binding is also what keeps it alive between the two starts below.
        service = bindService()
    }

    @After
    fun tearDown() {
        context.unbindService(connection)
        context.stopService(Intent(context, MusicService::class.java))
    }

    /** The queue empties out under the screen, so there is nothing left for it to control. */
    @Test
    fun leavesThePlayerScreenWhenTheQueueEmpties() {
        showPlayerScreen()

        start(listOf(missingSong()))

        assertEquals("the screen stayed up with no player behind it", 1, awaitBack(timeoutMs))
    }

    /**
     * The same failure with a song left over is a recovery, not an empty queue: retry() has already
     * started the next one by the time the screen hears about the error, and the screen stays.
     *
     * Waiting for the playback that recovery produces is what makes the assertion below more than a
     * sleep - the screen is given the whole retry, and then some, to leave.
     */
    @Test
    fun staysOnThePlayerScreenWhenTheRetryHasAnotherSong() {
        showPlayerScreen()

        val errored = errorLatch()

        start(listOf(missingSong(), realSong))

        assertTrue("the missing song never raised an error", errored.await(timeoutMs, TimeUnit.MILLISECONDS))
        awaitPlaying()

        assertEquals("the screen left although the retry had another song to play", 0, awaitBack(settleMs))
    }

    /** Renders the screen and plays a song on it, so its listener is registered and painting. */
    private fun showPlayerScreen() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides navigator) {
                PlayerScreen()
            }
        }

        start(listOf(realSong))

        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(realSong.name).fetchSemanticsNodes().isNotEmpty() }
    }

    /** An id no MediaStore row can have, so opening it fails with ERROR_CODE_IO_FILE_NOT_FOUND. */
    private fun missingSong(): SongModel {
        return SongModel(SongId("999999998"), missingName, ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun errorLatch(): CountDownLatch {
        val latch = CountDownLatch(1)

        service.addEventListener(object : MusicServiceListener {
            override fun onError() {
                latch.countDown()
            }
        })

        return latch
    }

    /** ExoPlayer is built on the main looper, so everything that touches it has to go there. */
    private fun start(songs: List<SongModel>) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { service.start(songs, 0) }
    }

    /** How many times the screen asked to be closed, within [timeout]. Returns early on the first. */
    private fun awaitBack(timeout: Long): Int {
        val deadline = SystemClock.uptimeMillis() + timeout

        while (SystemClock.uptimeMillis() < deadline) {
            if (navigator.backCount.get() > 0) break

            Thread.sleep(pollMs)
        }

        composeTestRule.waitForIdle()

        return navigator.backCount.get()
    }

    private fun awaitPlaying() {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            if (service.isPlaying()) return

            Thread.sleep(pollMs)
        }

        throw AssertionError("the retry never started the song that is still there")
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

    /** back() is the whole assertion; counted rather than flagged so a second call is visible. */
    private class RecordingNavigator : INavigator {
        val backCount = AtomicInteger(0)

        override fun back() {
            backCount.incrementAndGet()
        }

        override fun mainEdit() {}
        override fun artistDetail(id: String) {}
        override fun albumDetail(id: String) {}
        override fun favoriteArtistsEdit() {}
        override fun favoriteSongsEdit() {}
        override fun playlistsEdit() {}
        override fun playlistDetail(id: String) {}
        override fun playlistDetailEdit(id: String) {}
        override fun player() {}
    }
}
