package dev.tcode.thinmp.player

import android.content.Context
import android.os.Bundle
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A MediaController creates the service by binding to it, and a service that is only bound is
 * destroyed with its last binding. MediaSessionService starts itself only once playback is under
 * way, so a song that is still loading, or that fails to load, would go away with the connection
 * that queued it. Playback has to outlive the screen that started it.
 *
 * The song is a missing one, so nothing here needs MediaStore content and the test never skips
 * itself.
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceLifetimeTest {
    private val timeoutMs = 15_000L

    /** How long a destruction that must not happen is given to happen anyway. */
    private val quietMs = 1_000L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        // Starting a service is refused while the app is in the background.
        scenario = ActivityScenario.launch(MainActivity::class.java)
        // The activity's own connection would keep the service alive by itself.
        runOnMain { PlaybackController.from(context).release() }
    }

    @After
    fun tearDown() {
        scenario.close()
        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
    }

    @Test
    fun theServiceOutlivesTheConnectionThatQueuedASong() {
        val songRemoved = CountDownLatch(1)
        val controller = connectController(context, timeoutMs, object : MediaController.Listener {
            override fun onCustomCommand(controller: MediaController, command: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
                if (command.customAction == MusicService.SONG_REMOVED.customAction) songRemoved.countDown()

                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        })

        startOnController(controller, listOf(missingSong("999999997")), 0)
        // The song failing to load is proof the service has taken the queue, and it is the case
        // where playback never gets under way.
        assertTrue("the missing song was never dropped", songRemoved.await(timeoutMs, TimeUnit.MILLISECONDS))

        val watcher = MusicServiceWatcher.attach(context, timeoutMs)

        runOnMain { controller.release() }

        assertFalse("the service went away with the connection", watcher.awaitDestroyed(quietMs))
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
