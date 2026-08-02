package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.SystemClock
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
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A song whose file has gone missing raises ERROR_CODE_IO_FILE_NOT_FOUND, and the service drops it
 * and starts again on the next one. That recovery used to be unreachable whenever the *first* song
 * of the selection was the missing one: isStarting was still set from the start that had just
 * failed - playback never began, so nothing cleared it - and start() returns immediately while it
 * is set. The service was then left holding a released player and a flag nothing would clear, so
 * every later start() returned too.
 *
 * Both tests here end in "the real song is playing", which is the only observation that
 * distinguishes recovery from the lock-up. Needs at least one audio file on the device; skipped
 * otherwise (tools/push-test-audio.sh).
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceRetryTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var connection: ServiceConnection
    private lateinit var service: MusicService
    private lateinit var realSong: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        val songs = runBlocking { SongRepository(context).findAll() }
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        realSong = songs.first()

        // start() calls startForeground(), which the platform refuses while the app is in the
        // background, so the app has to actually be on screen for this to be testable at all.
        scenario = ActivityScenario.launch(MainActivity::class.java)
        service = bindService()
    }

    @After
    fun tearDown() {
        context.unbindService(connection)
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
    }

    /** The failed song is dropped and the one after it plays. */
    @Test
    fun playsTheNextSongWhenTheFirstOneIsMissing() {
        start(listOf(missingSong("999999998"), realSong), 0)

        waitUntilPlaying()
        assertEquals(realSong.id, currentSongId())
    }

    /** The retry chain has to keep going, not just survive one failure. */
    @Test
    fun playsTheFirstSongThatIsStillThere() {
        start(listOf(missingSong("999999998"), missingSong("999999997"), realSong), 0)

        waitUntilPlaying()
        assertEquals(realSong.id, currentSongId())
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    /** ExoPlayer is built on the main looper, so everything that touches it has to go there. */
    private fun start(songs: List<SongModel>, index: Int) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { service.start(songs, index) }
    }

    private fun currentSongId(): String? {
        var id: String? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { id = service.getCurrentSong()?.id }

        return id
    }

    private fun waitUntilPlaying() {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            if (service.isPlaying()) return

            Thread.sleep(pollMs)
        }

        fail("playback never started: the service is still holding the failed start")
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
}
