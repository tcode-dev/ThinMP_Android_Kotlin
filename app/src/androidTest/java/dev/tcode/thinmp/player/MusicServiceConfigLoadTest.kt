package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * onCreate() no longer blocks on DataStore, so the repeat and shuffle fields hold their defaults
 * for as long as the two reads take, and the buttons that drive them are on screen the whole time.
 * A tap in that window used to be undone by the stored value arriving behind it.
 *
 * The service is bound rather than started, so nothing here plays audio, needs MediaStore content
 * or reaches startForeground().
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceConfigLoadTest {
    /**
     * Whether a tap really lands inside the window is not ours to decide: it is posted from
     * onServiceConnected(), the earliest main thread callback a bind offers, and the DataStore read
     * runs against it. Losing that race only means the attempt proves nothing, never that it fails,
     * so the test takes several. One is enough to catch the overwrite.
     */
    private val attempts = 5
    private val timeoutMs = 15_000L
    private val quietMs = 1_000L

    private lateinit var context: Context
    private lateinit var config: ConfigStore
    private var connection: ServiceConnection? = null

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        config = ConfigStore(context)

        awaitServiceGone()
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

            val service = bind {
                it.changeRepeat()
                it.changeShuffle()
            }

            delay(quietMs)

            assertEquals("the stored repeat overwrote the tap on attempt $attempt", RepeatState.ALL, onMain { service.getRepeat() })
            assertTrue("the stored shuffle overwrote the tap on attempt $attempt", onMain { service.getShuffle() })

            unbind()
            resetConfig()
        }
    }

    /** The load itself still does its job when there is nothing to protect. */
    @Test
    fun theStoredValuesAreAppliedWhenNothingIsTapped() = runBlocking {
        config.saveRepeat(RepeatState.ONE)
        config.saveShuffle(true)

        val service = bind {}

        await("the stored repeat was never applied") { onMain { service.getRepeat() } == RepeatState.ONE }
        await("the stored shuffle was never applied") { onMain { service.getShuffle() } }
    }

    private suspend fun resetConfig() {
        config.saveRepeat(RepeatState.OFF)
        config.saveShuffle(false)
    }

    /** [onConnected] runs on the main thread, from inside the connection callback itself. */
    private fun bind(onConnected: (MusicService) -> Unit): MusicService {
        val latch = CountDownLatch(1)
        var bound: MusicService? = null

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                val service = (binder as MusicService.MusicBinder).getService()

                bound = service
                onConnected(service)
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }

        this.connection = connection
        context.bindService(Intent(context, MusicService::class.java), connection, Context.BIND_AUTO_CREATE)
        assertTrue("the service did not bind", latch.await(timeoutMs, TimeUnit.MILLISECONDS))

        return bound!!
    }

    /**
     * Waits for the service to actually go away, not just for the unbind to be handed over. The
     * next bind would otherwise be answered by the instance this one is still tearing down, which
     * has loaded its config long ago.
     */
    private suspend fun unbind() {
        val connection = this.connection ?: return

        this.connection = null
        context.unbindService(connection)
        context.stopService(Intent(context, MusicService::class.java))
        awaitServiceGone()
    }

    private suspend fun awaitServiceGone() {
        await("the previous service is still running") { !MusicService.isServiceRunning }
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
