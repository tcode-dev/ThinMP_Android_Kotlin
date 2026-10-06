package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.SystemClock
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * What the notification, the lock screen and a headset reach: the service's MediaSession, through a
 * MediaController, rather than the app's own binder.
 *
 * The back and forward buttons there have to behave the way the app's do, which ExoPlayer on its
 * own does not: it stops at either end of the queue and greys "next" out on the last song. And a
 * repeat or shuffle change made there has to be saved like one made in the app.
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
    private lateinit var connection: ServiceConnection
    private lateinit var service: MusicService
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
        service = bindService()
        controller = connectController()
    }

    @After
    fun tearDown() = runBlocking {
        onMain { controller.release() }
        context.unbindService(connection)
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
        resetConfig()
    }

    @Test
    fun nextOnTheLastSongGoesToTheFirst() {
        val songs = twoSongs()

        start(songs, 1)
        awaitIndex(1)

        onMain { controller.seekToNext() }

        awaitIndex(0)
    }

    /** The media notification's button sends the media item variant. */
    @Test
    fun nextMediaItemOnTheLastSongGoesToTheFirst() {
        val songs = twoSongs()

        start(songs, 1)
        awaitIndex(1)

        onMain { controller.seekToNextMediaItem() }

        awaitIndex(0)
    }

    /** Without the rewritten commands the button is greyed out here and can never be pressed. */
    @Test
    fun nextIsAvailableOnTheLastSong() {
        val songs = twoSongs()

        start(songs, 1)
        awaitIndex(1)

        await("next is not available on the last song") {
            onMain { controller.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT) && controller.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM) }
        }
    }

    @Test
    fun previousOnTheFirstSongGoesToTheLast() {
        val songs = twoSongs()

        start(songs, 0)
        awaitIndex(0)

        onMain { controller.seekToPrevious() }

        awaitIndex(1)
    }

    @Test
    fun previousMediaItemOnTheFirstSongGoesToTheLast() {
        val songs = twoSongs()

        start(songs, 0)
        awaitIndex(0)

        onMain { controller.seekToPreviousMediaItem() }

        awaitIndex(1)
    }

    /** Past the first three seconds "previous" restarts the song, on the media item variant too. */
    @Test
    fun previousAfterThreeSecondsRestartsTheSong() {
        val songs = twoSongs()

        startPlaying(songs, 1)
        awaitIndex(1)
        await("the duration never became known") { onMain { controller.duration } > 0 }
        assumeTrue("needs a song longer than five seconds", onMain { controller.duration } > 5_000)
        await("the song never got past three seconds") { onMain { controller.currentPosition } > 3_500 }

        onMain { controller.seekToPreviousMediaItem() }

        await("the song was not restarted") { onMain { controller.currentPosition } < 3_000 }
        assertEquals(1, onMain { controller.currentMediaItemIndex })
    }

    @Test
    fun repeatChangedThroughTheSessionIsSaved() = runBlocking {
        onMain { controller.repeatMode = Player.REPEAT_MODE_ALL }

        awaitSuspending("the repeat mode was not saved") { config.getRepeat() == RepeatState.ALL }
        assertEquals(RepeatState.ALL, onMain { service.getRepeat() })
    }

    @Test
    fun shuffleChangedThroughTheSessionIsSaved() = runBlocking {
        onMain { controller.shuffleModeEnabled = true }

        awaitSuspending("the shuffle mode was not saved") { config.getShuffle() }
        assertTrue(onMain { service.getShuffle() })
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
            service.start(songs, index)
            service.pause()
        }
    }

    private fun startPlaying(songs: List<SongModel>, index: Int) {
        onMain { service.start(songs, index) }
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

    private fun connectController(): MediaController {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = onMain { MediaController.Builder(context, token).buildAsync() }

        return future.get(timeoutMs, TimeUnit.MILLISECONDS)
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

        context.bindService(MusicService.bindIntent(context), connection, Context.BIND_AUTO_CREATE)
        assertTrue("the service did not bind", latch.await(timeoutMs, TimeUnit.MILLISECONDS))

        return bound!!
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
