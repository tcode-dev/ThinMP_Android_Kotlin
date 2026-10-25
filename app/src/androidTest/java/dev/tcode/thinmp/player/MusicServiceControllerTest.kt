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
 * 通知、ロック画面、ヘッドセットからの操作を、それらと同じくMediaController経由で確かめる。
 *
 * 前へ・次へはExoPlayer標準の動きで、リピートが全曲でなければキューの両端で止まる。
 * リピートとシャッフルの変更は、アプリから操作したときと同じく保存されなければならない。
 *
 * シークのテストは端末に音声ファイルが2つ必要で、なければスキップする(tools/push-test-audio.sh)。
 * 設定のテストは再生しない。
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

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        resetConfig()

        // 再生でサービスがフォアグラウンドになるため、アプリを前面に出しておく。
        // バックグラウンドからのフォアグラウンド化はプラットフォームに拒否される。
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

    /**
     * 前の曲がなければ、最初の3秒以内でも「前へ」は曲の先頭に戻る。
     */
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
     * 3秒を過ぎると「前へ」は曲の先頭に戻る。
     * 通知やヘッドセットのボタンはKEYCODE_MEDIA_PREVIOUSからseekToPrevious()として届くので、
     * それを使う。
     * seekToPreviousMediaItem()には3秒の判定がない。
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
     * 開始と同じメインスレッドのターンで一時停止し、実際には鳴らさない。
     * テスト用の曲は2秒しかなく、鳴らしたままだと自動で次の曲へ進む(末尾からは先頭へ戻る)ため、
     * ボタンを押した結果と区別できなくなる。
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

    /**
     * コントローラーはメインルーパーで作るので、操作はすべてメインスレッドで行う。
     */
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
