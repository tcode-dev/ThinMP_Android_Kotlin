package dev.tcode.thinmp.view.screen

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 失敗した曲が最後の1曲だった場合、retry()はキューを空にする。
 * そのあとプレーヤー画面のボタンを押してもmedia3は例外も出さずに無視するだけなので、
 * 画面には操作できるものがない。
 * ミニプレーヤーはこの状態で消えるので、プレーヤー画面も閉じなければならない。
 *
 * 画面が閉じるのは「曲がある」状態から「曲がない」状態になったときだけなので、
 * どちらのテストもまず実在の曲を再生し、画面にタイトルが出たのを確かめてから失敗する曲を入れる。
 * そのため端末に音声ファイルが必要で、なければスキップする(tools/push-test-audio.sh)。
 */
@RunWith(AndroidJUnit4::class)
class PlayerScreenQueueEmptyTest {
    private val timeoutMs = 15_000L
    private val settleMs = 3_000L
    private val pollMs = 50L
    private val missingName = "missing"

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navigator = RecordingNavigator()

    private lateinit var context: Context
    private lateinit var realSong: SongModel
    private val errorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")

        // 画面はタイトルで確かめるので、失敗する曲と名前が違う曲を選ぶ。
        val song = runBlocking { SongRepository(context).findAll() }.firstOrNull { it.name != missingName }
        assumeTrue("needs at least one audio file in MediaStore", song != null)
        realSong = song!!

        // 画面が読むPlaybackControllerは、アプリではMainActivityが接続するが、ここにはない。
        // 接続を待って、前のテストの状態ではなくこのサービスの状態から始める。
        // この接続が、下のstartの間でサービスを生かしておく役目も持つ。
        awaitControllerConnected()
    }

    @After
    fun tearDown() {
        errorScope.cancel()
        InstrumentationRegistry.getInstrumentation().runOnMainSync { PlaybackController.from(context).release() }
        context.stopService(Intent(context, MusicService::class.java))
    }

    /**
     * 画面を開いたままキューが空になれば、画面を閉じる。
     */
    @Test
    fun leavesThePlayerScreenWhenTheQueueEmpties() {
        showPlayerScreen()

        start(listOf(missingSong()))

        assertEquals("the screen stayed up with no player behind it", 1, awaitBack(timeoutMs))
    }

    /**
     * 曲が残っていれば同じ失敗でも回復できる。
     * 画面がエラーを知る時点でretry()は次の曲を再生しているので、画面は閉じない。
     *
     * 回復後の再生まで待ってから確かめるので、retryが終わるまで画面に閉じる時間を十分与えている。
     */
    @Test
    fun staysOnThePlayerScreenWhenTheRetryHasAnotherSong() {
        showPlayerScreen()

        val errored = errorLatch()

        start(listOf(missingSong(), realSong))

        assertTrue("the missing song never raised an error", errored.await(timeoutMs, TimeUnit.MILLISECONDS))
        awaitPlaying()

        assertEquals("the screen left although the retry had another song to play", 0, awaitBack(settleMs))
    }

    /**
     * 画面を表示して曲を再生し、画面のリスナーが登録されて描画が始まった状態にする。
     */
    private fun showPlayerScreen() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides navigator) {
                PlayerScreen()
            }
        }

        start(listOf(realSong))

        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(realSong.name).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * MediaStoreに存在しえないidなので、開くとERROR_CODE_IO_FILE_NOT_FOUNDで失敗する。
     */
    private fun missingSong(): SongModel {
        return SongModel(SongId("999999998"), missingName, ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun errorLatch(): CountDownLatch {
        val latch = CountDownLatch(1)

        // songRemovedは購読前のイベントを残さないので、UNDISPATCHEDで直ちに購読し、
        // 続くstartより前に購読を始めておく。
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            errorScope.launch(start = CoroutineStart.UNDISPATCHED) {
                PlaybackController.from(context).songRemoved.first()
                latch.countDown()
            }
        }

        return latch
    }

    /**
     * ExoPlayerはメインルーパーで作るので、操作はすべてメインスレッドで行う。
     */
    private fun start(songs: List<SongModel>) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { PlaybackController.from(context).start(songs, 0) }
    }

    /**
     * [timeout]の間に画面が閉じようとした回数。
     * 1回目で待つのをやめる。
     */
    private fun awaitBack(timeout: Long): Int {
        val deadline = SystemClock.uptimeMillis() + timeout

        while (SystemClock.uptimeMillis() < deadline) {
            if (navigator.backCount.get() > 0) break

            Thread.sleep(pollMs)
        }

        composeTestRule.waitForIdle()

        return navigator.backCount.get()
    }

    private fun awaitPlaying() {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            if (PlaybackController.from(context).state.value.isPlaying) return

            Thread.sleep(pollMs)
        }

        throw AssertionError("the retry never started the song that is still there")
    }

    private fun awaitControllerConnected() {
        val controller = PlaybackController.from(context)
        var connected = false
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        InstrumentationRegistry.getInstrumentation().runOnMainSync { controller.connect() }

        while (!connected) {
            assertTrue("the controller never connected", SystemClock.uptimeMillis() < deadline)
            Thread.sleep(pollMs)
            InstrumentationRegistry.getInstrumentation().runOnMainSync { connected = controller.isConnected() }
        }
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }

    /**
     * back()の呼び出しで判定する。
     * 2回呼ばれたこともわかるよう、フラグではなく回数を数える。
     */
    private class RecordingNavigator : INavigator {
        val backCount = AtomicInteger(0)

        override fun back() {
            backCount.incrementAndGet()
        }

        override fun mainEdit() {}
        override fun artistDetail(id: String) {}
        override fun albumDetail(id: String) {}
        override fun favoriteArtistsEdit() {}
        override fun favoriteSongsEdit() {}
        override fun playlistsEdit() {}
        override fun playlistDetail(id: String) {}
        override fun playlistDetailEdit(id: String) {}
        override fun player() {}
    }
}
