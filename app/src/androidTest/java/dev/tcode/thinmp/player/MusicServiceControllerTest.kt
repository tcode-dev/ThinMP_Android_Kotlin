package dev.tcode.thinmp.player

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * What the notification, the lock screen and a headset reach: the service's MediaSession, through a
 * MediaController, rather than the app's own binder.
 *
 * The back and forward buttons there are ExoPlayer's own: they stop at either end of the queue
 * unless repeat is set to all, and "next" is greyed out on the last song otherwise. And a repeat or
 * shuffle change made there has to be saved like one made in the app.
 *
 * The seek tests need two audio files on the device and skip themselves otherwise
 * (tools/push-test-audio.sh). The config tests play nothing.
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceControllerTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var config: ConfigStore
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: MediaController

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        config = ConfigStore(context)
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        resetConfig()

        // Playback puts the service in the foreground, which the platform refuses while the app is
        // in the background.
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = connectController(context, timeoutMs)
    }

    @After
    fun tearDown() = runBlocking {
        onMain { controller.release() }
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
        resetConfig()
    }

    @Test
    fun nextIsNotAvailableOnTheLastSong() {
        val songs = twoSongs()

        start(songs, 1)
        awaitIndex(1)

        await("next is still available on the last song") {
            onMain { !controller.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT) && !controller.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM) }
        }
    }

    /** With no song before it, "previous" restarts the first song even inside the first three seconds. */
    @Test
    fun previousOnTheFirstSongRestartsIt() {
        val songs = twoSongs()

        start(songs, 0)
        awaitIndex(0)
        await("the song never became ready") { onMain { controller.playbackState } == Player.STATE_READY }
        onMain { controller.seekTo(1_000) }
        await("the seek never landed") { onMain { controller.currentPosition } >= 1_000 }

        onMain { controller.seekToPrevious() }

        await("the song was not restarted") { onMain { controller.currentPosition } < 1_000 }
        assertEquals(0, onMain { controller.currentMediaItemIndex })
    }

    @Test
    fun nextOnTheLastSongGoesToTheFirstWithRepeatAll() {
        val songs = twoSongs()

        onMain { controller.repeatMode = Player.REPEAT_MODE_ALL }
        start(songs, 1)
        awaitIndex(1)

        onMain { controller.seekToNext() }

        awaitIndex(0)
    }

    @Test
    fun previousOnTheFirstSongGoesToTheLastWithRepeatAll() {
        val songs = twoSongs()

        onMain { controller.repeatMode = Player.REPEAT_MODE_ALL }
        start(songs, 0)
        awaitIndex(0)

        onMain { controller.seekToPrevious() }

        awaitIndex(1)
    }

    /**
     * Past the first three seconds "previous" restarts the song. seekToPrevious() is what the
     * notification's button and a headset's arrive as, both through KEYCODE_MEDIA_PREVIOUS;
     * seekToPreviousMediaItem() always moves to the song before.
     */
    @Test
    fun previousAfterThreeSecondsRestartsTheSong() {
        val songs = twoSongs()

        startPlaying(songs, 1)
        awaitIndex(1)
        await("the duration never became known") { onMain { controller.duration } > 0 }
        assumeTrue("needs a song longer than five seconds", onMain { controller.duration } > 5_000)
        await("the song never got past three seconds") { onMain { controller.currentPosition } > 3_500 }

        onMain { controller.seekToPrevious() }

        await("the song was not restarted") { onMain { controller.currentPosition } < 3_000 }
        assertEquals(1, onMain { controller.currentMediaItemIndex })
    }

    @Test
    fun repeatChangedThroughTheSessionIsSaved() = runBlocking {
        onMain { controller.repeatMode = Player.REPEAT_MODE_ALL }

        awaitSuspending("the repeat mode was not saved") { config.getRepeat() == RepeatState.ALL }
        assertEquals(Player.REPEAT_MODE_ALL, onMain { controller.repeatMode })
    }

    @Test
    fun shuffleChangedThroughTheSessionIsSaved() = runBlocking {
        onMain { controller.shuffleModeEnabled = true }

        awaitSuspending("the shuffle mode was not saved") { config.getShuffle() }
        assertTrue(onMain { controller.shuffleModeEnabled })
    }

    private fun twoSongs(): List<SongModel> {
        val songs = runBlocking { SongRepository(context).findAll() }

        assumeTrue("needs at least two audio files in MediaStore", songs.size >= 2)

        return songs.take(2)
    }

    private suspend fun resetConfig() {
        config.saveRepeat(RepeatState.OFF)
        config.saveShuffle(false)
    }

    /**
     * Paused straight away, in the same main thread turn, so the song never plays. The test songs
     * are two seconds long: left playing they move on to the next song, and back to the first one
     * after the last, by themselves, which is exactly where the buttons under test are meant to go.
     */
    private fun start(songs: List<SongModel>, index: Int) {
        onMain {
            startOnPlayer(songs, index)
            controller.pause()
        }
    }

    private fun startPlaying(songs: List<SongModel>, index: Int) {
        onMain { startOnPlayer(songs, index) }
    }

    private fun awaitIndex(index: Int) {
        await("never reached song $index") { onMain { controller.currentMediaItemIndex } == index }
    }

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, SystemClock.uptimeMillis() < deadline)
            Thread.sleep(pollMs)
        }
    }

    private suspend fun awaitSuspending(message: String, condition: suspend () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, SystemClock.uptimeMillis() < deadline)
            kotlinx.coroutines.delay(pollMs)
        }
    }

    /** The controller is built on the main looper, so everything that touches it has to go there. */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun startOnPlayer(songs: List<SongModel>, index: Int) {
        controller.setMediaItems(songs.map { it.toMediaItem(withUri = false) }, index, 0)
        controller.prepare()
        controller.play()
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
