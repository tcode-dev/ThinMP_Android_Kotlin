package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.MusicServiceWatcher
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Counts the reloads instead of querying anything. Public with an Application constructor so the factory can build it. */
class CountingSongListViewModel(application: Application) : SongListViewModel(application) {
    var loads = 0

    override val songs: List<SongModel> = emptyList()

    override fun load() {
        loads++
    }
}

/**
 * The song lists read PlaybackController rather than binding the service: they reload when the
 * service drops a song it could not play, and show the mini player from the state the controller
 * already holds. The activity connects the controller, as it does in the app.
 */
@RunWith(AndroidJUnit4::class)
class SongListViewModelTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: PlaybackController
    private val store = ViewModelStore()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        // Playback puts the service in the foreground, which the platform refuses while the app is
        // in the background.
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = PlaybackController.from(context)
        await("the controller never connected") { onMain { controller.isConnected() } }
    }

    @After
    fun tearDown() {
        onMain {
            store.clear()
            controller.pause()
        }
        scenario.close()
        context.stopService(Intent(context, MusicService::class.java))
    }

    /** Plays nothing real, so it needs no MediaStore content and never skips itself. */
    @Test
    fun reloadsWhenTheServiceDropsASong() {
        val viewModel = onMain { create(CountingSongListViewModel::class.java) }

        onMain { controller.start(listOf(missingSong("999999998")), 0) }

        await("the list was not reloaded after the song was dropped") { onMain { viewModel.loads } > 0 }
    }

    /**
     * A list opened for the first time while a song is queued, the way a child screen is. It used to
     * show the mini player only once its own bind had connected. Needs an audio file on the device;
     * skipped otherwise (tools/push-test-audio.sh).
     */
    @Test
    fun aListOpenedWhileASongIsQueuedShowsTheMiniPlayerFromTheStart() {
        val song = runBlocking { SongRepository(context).findAll() }.firstOrNull()
        assumeTrue("needs at least one audio file in MediaStore", song != null)

        onMain { controller.start(listOf(song!!), 0) }
        await("the song never reached the queue") { onMain { controller.state.value.hasQueue } }

        val isVisible = onMain { create(SongsViewModel::class.java).isVisiblePlayer.value }

        assertTrue("the mini player was hidden on the list's first frame", isVisible)
    }

    /** Main thread only, like the screens that build these. */
    private fun <T : ViewModel> create(modelClass: Class<T>): T {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context as Application)

        return ViewModelProvider(store, factory)[modelClass]
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, SystemClock.uptimeMillis() < deadline)
            Thread.sleep(pollMs)
        }
    }

    /** The controller and the view models run on the main thread, so everything goes there. */
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
