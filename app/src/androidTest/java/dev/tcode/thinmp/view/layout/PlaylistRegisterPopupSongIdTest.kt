package dev.tcode.thinmp.view.layout

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CommonLayoutViewが持つ「ポップアップを開いた曲のid」は、
 * CommonLayoutViewが再composeされても失われてはいけない。
 * SongId("")に戻ると、どの曲にも解決しない行がplaylist_songsに書き込まれ、
 * 追加したはずの曲が次の読み込みで消える。
 *
 * 再composeのきっかけは、ポップアップを開いている間に再生開始などでisVisibleMiniPlayerが切り替わること。
 * 2つのテストはその切り替えの有無だけが違うので、どちらが失敗したかで、追加そのものが壊れているのか、
 * 再composeの後だけなのかがわかる。
 *
 * ポップアップは自分でPlaylistsViewModelを作るので、インメモリではなくアプリのデータベースを使い、
 * テストの前後でプレイリストを消す。
 * アプリが削除しない行になるよう、MediaStoreに実在する曲idを使う。
 * 端末に音声ファイルがなければスキップする(tools/push-test-audio.sh)。
 */
@RunWith(AndroidJUnit4::class)
class PlaylistRegisterPopupSongIdTest {
    private val timeoutMs = 10_000L
    private val pollMs = 50L
    private val playlistName = "popup"

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private var songIdValue: String = ""

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(application.packageName, "android.permission.READ_MEDIA_AUDIO")
        clear()

        val songs = SongRepository(application).findAll()
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        songIdValue = songs.first().songId.id
    }

    @After
    fun tearDown() = runBlocking {
        clear()
    }

    @Test
    fun addsTheSongThePopupWasOpenedFor() {
        assertEquals(listOf(songIdValue), addFromPopup(recompose = false))
    }

    @Test
    fun addsTheSongThePopupWasOpenedForAfterTheLayoutRecomposes() {
        assertEquals(listOf(songIdValue), addFromPopup(recompose = true))
    }

    /**
     * 空のプレイリストを作ってポップアップを開き、[recompose]ならisVisibleMiniPlayerを切り替えてから
     * プレイリストをタップし、playlist_songsに書き込まれた曲を返す。
     */
    private fun addFromPopup(recompose: Boolean): List<String> {
        val songId = SongId(songIdValue)
        val playlistId = runBlocking {
            val repository = PlaylistRepository()
            repository.create(songId, playlistName)
            val playlist = repository.findAll().first { it.name == playlistName }
            // create()は曲を1つ入れて作るので、
            // その行を消してポップアップの書き込みだけが残るようにする。
            // 残したままだと、空のidが書き込まれても気づけない。
            repository.delete(PlaylistId(playlist.id), listOf(songId))
            PlaylistId(playlist.id)
        }
        val isVisibleMiniPlayer = mutableStateOf(false)

        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides NoNavigator) {
                CommonLayoutView(isVisibleMiniPlayer.value) { showPlaylistRegisterPopup ->
                    Text("open", modifier = Modifier.clickable { showPlaylistRegisterPopup(songId) })
                }
            }
        }

        composeTestRule.onNodeWithText("open").performClick()
        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(playlistName).fetchSemanticsNodes().isNotEmpty() }

        if (recompose) {
            composeTestRule.runOnUiThread { isVisibleMiniPlayer.value = true }
            composeTestRule.waitForIdle()
        }

        composeTestRule.onNodeWithText(playlistName).performClick()

        return awaitSongs(playlistId)
    }

    /** addSong()はviewModelScopeで動くので、行はタップの後から書き込まれる。 */
    private fun awaitSongs(playlistId: PlaylistId): List<String> {
        val deadline = System.currentTimeMillis() + timeoutMs

        while (System.currentTimeMillis() < deadline) {
            val songs = runBlocking { PlaylistRepository().findSongsByPlaylistId(playlistId) }

            if (songs.isNotEmpty()) {
                return songs.map { it.songId }
            }

            Thread.sleep(pollMs)
        }

        return emptyList()
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
