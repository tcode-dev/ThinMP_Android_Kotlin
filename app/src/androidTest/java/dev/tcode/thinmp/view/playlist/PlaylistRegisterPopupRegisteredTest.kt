package dev.tcode.thinmp.view.playlist

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * その曲がすでに入っているプレイリストには追加できないので、ポップアップは登録済みと表示し、
 * タップできないようにする。
 * そうしないと他の行と同じに見え、タップしても何も起きずに閉じるだけになる。
 *
 * タップが効いたかどうかはポップアップが閉じるかで判断する。
 * 行のタップは曲を追加してからコールバックを呼ぶが、重複は主キーで無視されるので、
 * 書き込みの有無では判断できない。
 *
 * ポップアップは自分でPlaylistsViewModelを作るので、インメモリではなくアプリのデータベースを使い、
 * テストの前後でプレイリストを消す。
 * 曲idをMediaStoreで引くことはないので、音声ファイルは要らない。
 */
@RunWith(AndroidJUnit4::class)
class PlaylistRegisterPopupRegisteredTest {
    private val timeoutMs = 10_000L
    private val registeredName = "registered"
    private val otherName = "other"
    private val songId = SongId("1")

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var registeredText: String

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        registeredText = application.getString(R.string.already_added)
        clear()
        // create()は曲を1つ入れて作るので、1つ目はsongIdを含むプレイリスト、2つ目は含まないプレイリストになる。
        val repository = PlaylistRepository()
        repository.create(songId, registeredName)
        repository.create(SongId("2"), otherName)
    }

    @After
    fun tearDown() = runBlocking {
        clear()
    }

    @Test
    fun marksThePlaylistTheSongIsAlreadyIn() {
        showPopup()

        awaitRegisteredMark()

        composeTestRule.onNodeWithText(registeredText).assertExists()
    }

    @Test
    fun doesNotRespondToATapOnThePlaylistTheSongIsAlreadyIn() {
        var closed = false

        showPopup { closed = true }

        awaitRegisteredMark()

        composeTestRule.onNodeWithText(registeredName).performClick()
        composeTestRule.waitForIdle()

        assertFalse(closed)
    }

    @Test
    fun stillAddsToAPlaylistTheSongIsNotIn() {
        var closed = false

        showPopup { closed = true }

        composeTestRule.onNodeWithText(otherName).performClick()
        composeTestRule.waitForIdle()

        assertTrue(closed)
    }

    private fun showPopup(callback: () -> Unit = {}) {
        composeTestRule.setContent {
            PlaylistRegisterPopupView(songId, callback = callback)
        }

        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(otherName).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * 登録済みかどうかは一覧とは別に読み込むので、一覧が先に表示され、印は後から付く。
     * 印が付いてからタップしなければならない。
     */
    private fun awaitRegisteredMark() {
        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(registeredText).fetchSemanticsNodes().isNotEmpty() }
    }

    private suspend fun clear() {
        val repository = PlaylistRepository()

        repository.findAll().forEach { repository.delete(PlaylistId(it.id)) }
    }
}
