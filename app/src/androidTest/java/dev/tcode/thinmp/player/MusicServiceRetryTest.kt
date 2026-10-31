package dev.tcode.thinmp.player

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.media3.common.C
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
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
 * 再生中に失敗するテストはそれで終わる。
 * 一時停止中に失敗した場合は、再生を始めずに次の曲に移らなければならない。
 * 次の曲はシャッフル順で決まり、キューの並び順ではない。
 *
 * 端末に音声ファイルが必要で、なければスキップする(tools/push-test-audio.sh)。
 * 一時停止とシャッフルのテストは2つ必要。
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceRetryTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var config: ConfigStore
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: MediaController
    private lateinit var songs: List<SongModel>
    private lateinit var realSong: SongModel

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        config = ConfigStore(context)
        grantPermission("android.permission.READ_MEDIA_AUDIO")

        songs = SongRepository(context).findAll()
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        realSong = songs.first()

        // シャッフルは保存され、サービスは起動時にそれを読む。
        // 前のテストのサービスが残っていると、リセットした設定を読まない。
        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        resetConfig()

        // 再生でサービスがフォアグラウンドになるため、アプリを前面に出しておく。
        // バックグラウンドからのフォアグラウンド化はプラットフォームに拒否される。
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = connectController(context, timeoutMs)
    }

    @After
    fun tearDown() = runBlocking {
        runOnMain { controller.release() }
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
        resetConfig()
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

    /**
     * 一時停止中に失敗した曲に当たっても、一時停止のまま次の曲に移る。
     */
    @Test
    fun staysPausedWhenItSkipsAMissingSong() {
        val (first, second) = twoSongs()

        prepare(listOf(first, missingSong("999999998"), second))
        runOnMain { controller.seekToNextMediaItem() }

        awaitSettled(2)
        assertEquals(second.id, runOnMain { controller.currentMediaItem?.mediaId })
        assertFalse(runOnMain { controller.playWhenReady })
    }

    /**
     * シャッフル中は、外した曲のシャッフル順で次の曲に移る。
     * シャッフル順が最後なら1つ前の曲に移る。
     *
     * シャッフル順は無作為なので、キューの並び順と結果が違う順になるまで並べ直す。
     * 同じ結果になる順では、キューの並び順で決めていても通ってしまう。
     */
    @Test
    fun skipsAMissingSongInShuffleOrder() {
        val (first, second) = twoSongs()
        val queue = listOf(first, missingSong("999999998"), second)
        var expected: String? = null

        runOnMain { controller.shuffleModeEnabled = true }

        repeat(30) {
            if (expected != null) return@repeat

            prepare(queue)

            val timeline = runOnMain { controller.currentTimeline }
            val next = timeline.getNextWindowIndex(1, Player.REPEAT_MODE_OFF, true).takeIf { it != C.INDEX_UNSET }
                ?: timeline.getPreviousWindowIndex(1, Player.REPEAT_MODE_OFF, true)

            if (next != 2) expected = queue[next].id
        }

        assertTrue("the shuffle order never differed from the queue order", expected != null)

        runOnMain { controller.seekTo(1, 0) }

        awaitSettled(2)
        assertEquals(expected, runOnMain { controller.currentMediaItem?.mediaId })
    }

    private fun twoSongs(): Pair<SongModel, SongModel> {
        assumeTrue("needs at least two audio files in MediaStore", songs.size >= 2)

        return songs[0] to songs[1]
    }

    /**
     * 再生せずに先頭の曲を読み込み、サービスのタイムラインが届くまで待つ。
     * READYはサービスからしか届かないので、その時点のタイムラインはサービスのシャッフル順を持つ。
     */
    private fun prepare(songs: List<SongModel>) {
        runOnMain {
            controller.setMediaItems(songs.map { it.toMediaItem(withUri = false) }, 0, 0)
            controller.prepare()
        }

        await("the first song never became ready") {
            runOnMain { controller.mediaItemCount == songs.size && controller.playbackState == Player.STATE_READY }
        }
    }

    /**
     * 失敗した曲が外れ、次の曲の読み込みが終わるまで待つ。
     */
    private fun awaitSettled(count: Int) {
        await("the missing song was never removed") {
            runOnMain { controller.mediaItemCount == count && controller.playbackState == Player.STATE_READY }
        }
    }

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, SystemClock.uptimeMillis() < deadline)
            Thread.sleep(pollMs)
        }
    }

    private suspend fun resetConfig() {
        config.saveRepeat(RepeatState.OFF)
        config.saveShuffle(false)
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
