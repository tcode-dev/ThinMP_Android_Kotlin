package dev.tcode.thinmp.view.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.SystemClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * MediaStore reports duration 0 for a file whose metadata could not be read, and the player screen
 * divided the position by it. The two tests are the two answers that division has: at position 0
 * it is NaN, which Compose cannot round while it lays the thumb out (`Cannot round NaN value`, and
 * the screen goes down with it), and from the next tick on it is +Infinity, which the Slider does
 * clamp - to the end of the track, so a song that had just started was drawn as a finished one.
 *
 * The song has to be a real one so it plays. Only the duration is replaced, which is what
 * MediaStore itself would have handed over for an untagged file. Needs an audio file on the
 * device; skipped otherwise (tools/push-test-audio.sh).
 */
@RunWith(AndroidJUnit4::class)
class PlayerScreenUnknownDurationTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    /** One seek bar interval and then some, so the periodic update is included in the assertion. */
    private val tickMs = 1_500L

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var connection: ServiceConnection
    private lateinit var service: MusicService
    private lateinit var untaggedSong: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        val song = runBlocking { SongRepository(context).findAll() }.firstOrNull()
        assumeTrue("needs at least one audio file in MediaStore", song != null)
        untaggedSong = withoutDuration(song!!)

        // The screen only binds while the service is already running, so it has to exist before
        // the composition starts.
        service = bindService()
    }

    @After
    fun tearDown() {
        context.unbindService(connection)
        context.stopService(Intent(context, MusicService::class.java))
    }

    /**
     * The screen painting while the position is still 0 is the NaN, and that is the state a song
     * is in for the first moments after it is tapped. Paused and sought back to the start rather
     * than raced against real playback, which passes 0 in a few milliseconds.
     */
    @Test
    fun opensOnASongOfUnknownDurationWithTheSliderAtTheStart() {
        start(listOf(untaggedSong))
        awaitPlaying()
        onMain {
            service.pause()
            service.seekTo(0)
        }

        showPlayerScreen()

        assertSliderAtTheStart()
    }

    /** Playing, so the periodic seek bar update runs: the second place the fraction is computed. */
    @Test
    fun playsASongOfUnknownDurationWithTheSliderAtTheStart() {
        start(listOf(untaggedSong))
        awaitPlaying()

        showPlayerScreen()

        Thread.sleep(tickMs)
        composeTestRule.waitForIdle()

        assertSliderAtTheStart()
    }

    /** The Slider is the only node on the screen carrying a range, so it needs no other selector. */
    private fun assertSliderAtTheStart() {
        composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertRangeInfoEquals(ProgressBarRangeInfo(0f, 0f..1f))
    }

    /** The title is the only proof from out here that the screen has bound and painted the song. */
    private fun showPlayerScreen() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides NoopNavigator()) {
                PlayerScreen()
            }
        }

        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(untaggedSong.name).fetchSemanticsNodes().isNotEmpty() }
    }

    /** The row MediaStore hands back when the file carries no length: everything else intact. */
    private fun withoutDuration(song: SongModel): SongModel {
        return SongModel(song.songId, song.name, song.artistId, song.artistName, song.albumId, song.albumName, 0, song.trackNumber)
    }

    private fun start(songs: List<SongModel>) {
        onMain { service.start(songs, 0) }
    }

    /** ExoPlayer is built on the main looper, so everything that touches it has to go there. */
    private fun onMain(block: () -> Unit) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
    }

    private fun awaitPlaying() {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            if (service.isPlaying()) return

            Thread.sleep(pollMs)
        }

        throw AssertionError("the song never started playing")
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

    /** The screen is only expected to stay where it is, so back() has nothing to record. */
    private class NoopNavigator : INavigator {
        override fun back() {}
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
