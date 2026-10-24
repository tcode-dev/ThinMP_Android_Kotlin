package dev.tcode.thinmp.view.screen

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertRangeInfoEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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

/**
 * メタデータを読めないファイルの長さはMediaStoreでは0になり、プレーヤー画面は再生位置をそれで割る。
 * 位置が0のときはNaNになり、Composeがつまみの配置で丸められずに落ちる(`Cannot round NaN value`)。
 * 位置が進むと+Infinityになり、Sliderは末尾に丸めるので、始まったばかりの曲が終わったように見える。
 * 2つのテストはこの2つのケースに対応する。
 *
 * 再生するので曲は実在するものを使い、長さだけをタグのないファイルと同じ0に差し替える。
 * 端末に音声ファイルが必要で、なければスキップする(tools/push-test-audio.sh)。
 */
@RunWith(AndroidJUnit4::class)
class PlayerScreenUnknownDurationTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    /** シークバーの更新間隔(1秒)より長くして、定期更新を少なくとも1回含める。 */
    private val tickMs = 1_500L

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var untaggedSong: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

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
        start(listOf(untaggedSong))
        awaitPlaying()
        onMain {
            PlaybackController.from(context).pause()
            PlaybackController.from(context).seekTo(0)
        }

        showPlayerScreen()

        assertSliderAtTheStart()
    }

    /** 再生中なので定期的なシークバーの更新が走る。割合を計算するもう1つの場所。 */
    @Test
    fun playsASongOfUnknownDurationWithTheSliderAtTheStart() {
        start(listOf(untaggedSong))
        awaitPlaying()

        showPlayerScreen()

        Thread.sleep(tickMs)
        composeTestRule.waitForIdle()

        assertSliderAtTheStart()
    }

    /** 範囲を持つノードは画面にSliderしかないので、他の条件で絞り込まない。 */
    private fun assertSliderAtTheStart() {
        composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertRangeInfoEquals(ProgressBarRangeInfo(0f, 0f..1f))
    }

    /** 画面が曲を受け取って描画したことは、タイトルでしか確かめられない。 */
    private fun showPlayerScreen() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides NoopNavigator()) {
                PlayerScreen()
            }
        }

        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(untaggedSong.name).fetchSemanticsNodes().isNotEmpty() }
    }

    /** 長さのタグがないファイルについてMediaStoreが返す行。長さ以外はそのまま。 */
    private fun withoutDuration(song: SongModel): SongModel {
        return SongModel(song.songId, song.name, song.artistId, song.artistName, song.albumId, song.albumName, 0, song.trackNumber)
    }

    private fun start(songs: List<SongModel>) {
        onMain { PlaybackController.from(context).start(songs, 0) }
    }

    /** ExoPlayerはメインルーパーで作るので、操作はすべてメインスレッドで行う。 */
    private fun onMain(block: () -> Unit) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
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

    /** 画面が閉じないことだけを確かめるので、back()は何も記録しない。 */
    private class NoopNavigator : INavigator {
        override fun back() {}
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
