package dev.tcode.thinmp.player

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.media3.session.MediaController
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
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ファイルが消えた曲はERROR_CODE_IO_FILE_NOT_FOUNDになり、サービスはその曲を外して次の曲を再生する。
 * 最初の曲が消えていて、エラーの時点で一度も再生が始まっていない場合も同じでなければならない。
 *
 * 回復したことは「実在する曲が再生中であること」でしか確かめられないので、
 * どちらのテストもそれで終わる。
 * 端末に音声ファイルが必要で、なければスキップする(tools/push-test-audio.sh)。
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceRetryTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: MediaController
    private lateinit var realSong: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")

        val songs = runBlocking { SongRepository(context).findAll() }
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        realSong = songs.first()

        // 再生でサービスがフォアグラウンドになるため、アプリを前面に出しておく。
        // バックグラウンドからのフォアグラウンド化はプラットフォームに拒否される。
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = connectController(context, timeoutMs)
    }

    @After
    fun tearDown() {
        runOnMain { controller.release() }
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
    }

    /**
     * 失敗した曲を外し、次の曲を再生する。
     */
    @Test
    fun playsTheNextSongWhenTheFirstOneIsMissing() {
        startOnController(controller, listOf(missingSong("999999998"), realSong), 0)

        waitUntilPlaying()
        assertEquals(realSong.id, runOnMain { controller.currentMediaItem?.mediaId })
    }

    /**
     * 失敗が続いても、再生できる曲まで進み続ける。
     */
    @Test
    fun playsTheFirstSongThatIsStillThere() {
        startOnController(controller, listOf(missingSong("999999998"), missingSong("999999997"), realSong), 0)

        waitUntilPlaying()
        assertEquals(realSong.id, runOnMain { controller.currentMediaItem?.mediaId })
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun waitUntilPlaying() {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            if (runOnMain { controller.isPlaying }) return

            Thread.sleep(pollMs)
        }

        fail("playback never started: the service is still holding the failed start")
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
