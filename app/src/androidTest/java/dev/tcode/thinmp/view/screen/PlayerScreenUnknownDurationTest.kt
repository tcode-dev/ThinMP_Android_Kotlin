package dev.tcode.thinmp.view.screen

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.HiltTestActivity
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * メタデータを読めないファイルの長さはMediaStoreでは0になる。
 * プレーヤー画面がそれで再生位置を割ると、位置が0のときはNaNになってComposeが落ち(`Cannot round NaN value`)、
 * 位置が進むと+Infinityになって始まったばかりの曲が終わったように見える。
 * シークも長さに割合を掛けるので、常に0msになる。
 * 画面はプレーヤーがファイルから知った長さを使うので、どれも起きないことを確かめる。
 *
 * 再生するので曲は実在するものを使い、長さだけをタグのないファイルと同じ0に差し替える。
 * 端末に音声ファイルが必要で、なければスキップする(tools/push-test-audio.sh)。
 * テスト用の曲は2秒なので、長さの表示は00:02になる。
 */
@RunWith(AndroidJUnit4::class)
class PlayerScreenUnknownDurationTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    /**
     * シークバーの更新間隔(1秒)より長くして、定期更新を少なくとも1回含める。
     */
    private val tickMs = 1_500L

    /**
     * コントローラーの位置はサービスの位置からの推定なので、多少ずれる。
     */
    private val seekToleranceMs = 200L
    private val durationText = "00:02"

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var context: Context
    private lateinit var untaggedSong: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")

        val song = runBlocking { SongRepository(context).findAll() }.firstOrNull()
        assumeTrue("needs at least one audio file in MediaStore", song != null)
        untaggedSong = withoutDuration(song!!)

        // 画面が読むPlaybackControllerは、アプリではMainActivityが接続するが、ここにはない。
        // 接続を待って、前のテストの状態ではなくこのサービスの状態から始める。
        // この接続が、下のstartの間でサービスを生かしておく役目も持つ。
        awaitControllerConnected()
    }

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { PlaybackController.from(context).release() }
        context.stopService(Intent(context, MusicService::class.java))
    }

    /**
     * 位置が0のまま画面を描画するケース(NaN)。
     * 曲をタップした直後はこの状態になる。
     * 実際の再生では数ミリ秒で0を過ぎてしまうので、一時停止して先頭にシークしておく。
     */
    @Test
    fun opensOnASongOfUnknownDurationWithTheSliderAtTheStart() {
        startPaused()

        showPlayerScreen()

        assertSliderAt(0f)
        composeTestRule.onNodeWithText(durationText).assertExists()
    }

    /**
     * 再生中なので定期的なシークバーの更新が走る。
     * 割合を計算するもう1つの場所。
     * MediaStoreの長さで割るとつまみは動かないので、進んでいることを確かめる。
     */
    @Test
    fun playsASongOfUnknownDurationWithTheSliderMoving() {
        start(listOf(untaggedSong))
        awaitPlaying()

        showPlayerScreen()

        Thread.sleep(tickMs)
        composeTestRule.waitForIdle()

        val position = sliderNode().fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current

        assertTrue("the slider never moved: $position", position > 0f && position <= 1f)
    }

    /**
     * MediaStoreの長さ(0)に割合を掛けると、どこを押しても先頭にシークする。
     * 一時停止しているので、シークした位置から動かない。
     */
    @Test
    fun seeksASongOfUnknownDurationByThePlayersDuration() {
        startPaused()
        showPlayerScreen()

        sliderNode().performSemanticsAction(SemanticsActions.SetProgress) { it(0.5f) }
        composeTestRule.waitForIdle()

        val duration = onMain { PlaybackController.from(context).duration() }
        val position = awaitPosition { it > 0 }

        assertTrue("the player knows no duration", duration > 0)
        assertTrue("seeked to $position of $duration", abs(position - duration / 2) <= seekToleranceMs)
    }

    private fun startPaused() {
        start(listOf(untaggedSong))
        awaitPlaying()
        onMain {
            PlaybackController.from(context).pause()
            PlaybackController.from(context).seekTo(0)
        }
        awaitPosition { it == 0L }
    }

    /**
     * 範囲を持つノードは画面にSliderしかないので、他の条件で絞り込まない。
     */
    private fun sliderNode() = composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))

    private fun assertSliderAt(position: Float) {
        sliderNode().assertRangeInfoEquals(ProgressBarRangeInfo(position, 0f..1f))
    }

    /**
     * 画面が曲を受け取って描画したことは、タイトルでしか確かめられない。
     */
    private fun showPlayerScreen() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides NoopNavigator()) {
                PlayerScreen()
            }
        }

        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(untaggedSong.name).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * 長さのタグがないファイルについてMediaStoreが返す行。
     * 長さ以外はそのまま。
     */
    private fun withoutDuration(song: SongModel): SongModel {
        return SongModel(song.songId, song.name, song.artistId, song.artistName, song.albumId, song.albumName, 0, song.trackNumber)
    }

    private fun start(songs: List<SongModel>) {
        onMain { PlaybackController.from(context).start(songs, 0) }
    }

    /**
     * シークはサービスに届いてから位置に表れるので、待つ。
     */
    private fun awaitPosition(condition: (Long) -> Boolean): Long {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            val position = onMain { PlaybackController.from(context).currentPosition() }

            if (condition(position)) return position

            Thread.sleep(pollMs)
        }

        throw AssertionError("the position never reached the expected value")
    }

    /**
     * ExoPlayerはメインルーパーで作るので、操作はすべてメインスレッドで行う。
     */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun awaitPlaying() {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            if (PlaybackController.from(context).state.value.isPlaying) return

            Thread.sleep(pollMs)
        }

        throw AssertionError("the song never started playing")
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
     * 画面が閉じないことだけを確かめるので、back()は何も記録しない。
     */
    private class NoopNavigator : INavigator {
        override fun back() {}
        override fun backFromEvent() {}
        override fun navigate(route: String) {}
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
