package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * onCreate() no longer blocks on DataStore, so the player's repeat and shuffle modes hold their
 * defaults for as long as the two reads take, and the buttons that drive them are on screen the whole time.
 * A tap in that window used to be undone by the stored value arriving behind it.
 *
 * The service is reached through a MediaController and only bound, so nothing here plays audio, needs MediaStore content
 * or reaches startForeground().
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceConfigLoadTest {
    /**
     * Whether a tap really lands inside the window is not ours to decide: it is sent from the
     * controller's connection callback, the earliest point a tap from the app could arrive, and the
     * DataStore read runs against it. Losing that race only means the attempt proves nothing, never that it fails,
     * so the test takes several. One is enough to catch the overwrite.
     */
    private val attempts = 5
    private val timeoutMs = 15_000L
    private val quietMs = 1_000L

    private lateinit var context: Context
    private lateinit var config: ConfigStore
    private var controller: MediaController? = null

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        config = ConfigStore(context)

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        resetConfig()
    }

    @After
    fun tearDown() = runBlocking {
        unbind()
        resetConfig()
    }

    /**
     * From the default OFF the first tap gives ALL, and shuffle goes to true, while the stored
     * values are OFF and false - so the load landing on top of the tap is the one outcome that is
     * distinguishable from every other ordering.
     */
    @Test
    fun tappingBeforeTheConfigLoadsKeepsTheTap() = runBlocking {
        for (attempt in 1..attempts) {
            assertEquals("attempt $attempt did not start from a stored OFF", RepeatState.OFF, config.getRepeat())
            assertFalse("attempt $attempt did not start from a stored false", config.getShuffle())

            val controller = connect {
                it.repeatMode = Player.REPEAT_MODE_ALL
                it.shuffleModeEnabled = true
            }

            delay(quietMs)

            assertEquals("the stored repeat overwrote the tap on attempt $attempt", Player.REPEAT_MODE_ALL, onMain { controller.repeatMode })
            assertTrue("the stored shuffle overwrote the tap on attempt $attempt", onMain { controller.shuffleModeEnabled })

            unbind()
            resetConfig()
        }
    }

    /** The load itself still does its job when there is nothing to protect. */
    @Test
    fun theStoredValuesAreAppliedWhenNothingIsTapped() = runBlocking {
        config.saveRepeat(RepeatState.ONE)
        config.saveShuffle(true)

        val controller = connect {}

        await("the stored repeat was never applied") { onMain { controller.repeatMode } == Player.REPEAT_MODE_ONE }
        await("the stored shuffle was never applied") { onMain { controller.shuffleModeEnabled } }
    }

    private suspend fun resetConfig() {
        config.saveRepeat(RepeatState.OFF)
        config.saveShuffle(false)
    }

    /** [onConnected] runs on the main thread, from inside the connection callback itself. */
    private fun connect(onConnected: (MediaController) -> Unit): MediaController {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        // Built and listened to in the same main thread turn, so the future cannot complete before
        // the listener is in place and the listener runs on the main thread that completes it.
        val future = onMain {
            MediaController.Builder(context, token).buildAsync().also { future ->
                future.addListener({ onConnected(future.get()) }, MoreExecutors.directExecutor())
            }
        }

        val controller = future.get(timeoutMs, TimeUnit.MILLISECONDS)

        this.controller = controller

        return controller
    }

    /**
     * Waits for the service to actually go away, not just for the connection to be let go. The
     * next connection would otherwise be answered by the instance this one is still tearing down,
     * which has loaded its config long ago.
     */
    private fun unbind() {
        val controller = this.controller ?: return

        this.controller = null
        onMain { controller.release() }
        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
    }

    private suspend fun await(message: String, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, System.currentTimeMillis() < deadline)
            delay(20)
        }
    }

    /** The fields are written from the service's own main thread coroutine, so they are read there. */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }
}
