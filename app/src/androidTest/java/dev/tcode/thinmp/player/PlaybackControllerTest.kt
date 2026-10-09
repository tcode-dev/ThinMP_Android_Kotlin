package dev.tcode.thinmp.player

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.viewModel.MiniPlayerUiState
import dev.tcode.thinmp.viewModel.MiniPlayerViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PlaybackController is what the screens read and drive instead of binding the service each. The
 * activity connects it, as it does in the app.
 *
 * Every test plays a real song, so each needs an audio file on the device and skips itself
 * otherwise (tools/push-test-audio.sh).
 */
@RunWith(AndroidJUnit4::class)
class PlaybackControllerTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: PlaybackController
    private lateinit var song: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        val songs = runBlocking { SongRepository(context).findAll() }
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        song = songs.first()

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        // Playback puts the service in the foreground, which the platform refuses while the app is
        // in the background.
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = PlaybackController.from(context)
        await("the controller never connected") { onMain { controller.isConnected() } }
    }

    @After
    fun tearDown() {
        onMain { controller.pause() }
        scenario.close()
        context.stopService(Intent(context, MusicService::class.java))
    }

    /**
     * The controller sends a song without its URI, and the service has to put it back in
     * onAddMediaItems() before the player can open the file.
     */
    @Test
    fun playsASongStartedThroughTheController() {
        onMain { controller.start(listOf(song), 0) }

        awaitPlaying()
    }

    /**
     * Started while there is no connection - between the activity's onStop() and the next
     * onStart(), or before the first connection completes - and run once it is up rather than
     * dropped.
     */
    @Test
    fun runsAStartMadeBeforeTheConnection() {
        onMain {
            controller.release()
            controller.start(listOf(song), 0)
            controller.connect()
        }

        awaitPlaying()
    }

    /**
     * A mini player on a screen opened after the song started, which is the state a child screen
     * is in. Each used to paint what it held from its own bind, or nothing until the bind arrived.
     */
    @Test
    fun aNewMiniPlayerHasTheCurrentSongFromTheStart() {
        onMain { controller.start(listOf(song), 0) }
        awaitPlaying()

        val store = ViewModelStore()

        try {
            val uiState = onMain {
                val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context as Application)

                ViewModelProvider(store, factory)[MiniPlayerViewModel::class.java].uiState.value
            }

            // The test songs are two seconds long, so whether it is still playing is left out.
            assertEquals(MiniPlayerUiState(song.name, song.getImageUri(), isVisible = true), uiState.copy(isPlaying = false))
        } finally {
            onMain { store.clear() }
        }
    }

    private fun awaitPlaying() {
        await("the song never started playing") {
            onMain { controller.state.value.let { it.isPlaying && it.currentSong?.id == song.id } }
        }
    }

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, SystemClock.uptimeMillis() < deadline)
            Thread.sleep(pollMs)
        }
    }

    /** The controller runs on the main thread, so everything that touches it goes there. */
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
