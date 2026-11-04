package dev.tcode.thinmp.view.screen

import android.app.Application
import android.content.Intent
import android.os.SystemClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.HiltTestActivity
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.MusicServiceWatcher
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * プレーヤー画面のプレイリストのポップアップは、開いたときの曲を追加する。
 * 開いている間に次の曲へ進んでも、追加するのは進む前の曲でなければならない。
 *
 * ポップアップは自分でPlaylistsViewModelを作るので、インメモリではなくアプリのデータベースを使い、
 * テストの前後でプレイリストを消す。
 * 端末に音声ファイルが2つ必要で、なければスキップする(tools/push-test-audio.sh)。
 */
@RunWith(AndroidJUnit4::class)
class PlayerScreenPlaylistPopupTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L
    private val playlistName = "popup"

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var application: Application
    private lateinit var first: SongModel
    private lateinit var second: SongModel

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(application.packageName, "android.permission.READ_MEDIA_AUDIO")

        val songs = SongRepository(application).findAll()
        assumeTrue("needs at least two audio files in MediaStore", songs.size >= 2)
        first = songs[0]
        second = songs[1]

        // シャッフルが残っていると、次の曲が2曲目にならないことがある。
        // サービスは起動時に設定を読むので、前のテストのサービスを止めてから戻す。
        MusicServiceWatcher.stopAndAwait(application, timeoutMs)
        resetConfig()
        clear()

        // 画面が読むPlaybackControllerは、アプリではMainActivityが接続するが、ここにはない。
        awaitControllerConnected()
    }

    @After
    fun tearDown() = runBlocking {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { PlaybackController.from(application).release() }
        application.stopService(Intent(application, MusicService::class.java))
        clear()
        resetConfig()
    }

    @Test
    fun addsTheSongThePopupWasOpenedForAfterTheNextSongStarts() {
        val playlistId = createEmptyPlaylist()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides NoNavigator) {
                PlayerScreen()
            }
        }

        // テスト用の曲は2秒しかないので、鳴らすと自動で次の曲へ進んでしまう。
        // 開始と同じメインスレッドのターンで一時停止する。
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val controller = PlaybackController.from(application)

            controller.start(listOf(first, second), 0)
            controller.pause()
        }
        awaitText(first.name)

        // アイコンにcontentDescriptionがないので、画面で最後のクリックできるノードとして探す。
        composeTestRule.onAllNodes(hasClickAction()).onLast().performClick()
        awaitText(playlistName)

        InstrumentationRegistry.getInstrumentation().runOnMainSync { PlaybackController.from(application).next() }
        awaitText(second.name)

        composeTestRule.onNodeWithText(playlistName).performClick()

        assertEquals(listOf(first.songId.id), awaitSongs(playlistId))
    }

    /**
     * create()は曲を1つ入れて作るので、その行を消してポップアップの書き込みだけが残るようにする。
     */
    private fun createEmptyPlaylist(): PlaylistId = runBlocking {
        val repository = PlaylistRepository()

        repository.create(first.songId, playlistName)

        val playlistId = PlaylistId(repository.findAll().first { it.name == playlistName }.id)

        repository.delete(playlistId, listOf(first.songId))

        playlistId
    }

    private fun awaitText(text: String) {
        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * addSong()はviewModelScopeで動くので、行はタップの後から書き込まれる。
     */
    private fun awaitSongs(playlistId: PlaylistId): List<String> {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (SystemClock.uptimeMillis() < deadline) {
            val songs = runBlocking { PlaylistRepository().findSongsByPlaylistId(playlistId) }

            if (songs.isNotEmpty()) return songs.map { it.songId }

            Thread.sleep(pollMs)
        }

        return emptyList()
    }

    private fun awaitControllerConnected() {
        val controller = PlaybackController.from(application)
        var connected = false
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        InstrumentationRegistry.getInstrumentation().runOnMainSync { controller.connect() }

        while (!connected) {
            assertTrue("the controller never connected", SystemClock.uptimeMillis() < deadline)
            Thread.sleep(pollMs)
            InstrumentationRegistry.getInstrumentation().runOnMainSync { connected = controller.isConnected() }
        }
    }

    private suspend fun resetConfig() {
        val config = ConfigStore(application)

        config.saveRepeat(RepeatState.OFF)
        config.saveShuffle(false)
    }

    private suspend fun clear() {
        val repository = PlaylistRepository()

        repository.findAll().forEach { repository.delete(PlaylistId(it.id)) }
    }

    private object NoNavigator : INavigator {
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
