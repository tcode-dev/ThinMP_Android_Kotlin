package dev.tcode.thinmp.view.screen

import android.app.Application
import android.content.ContentValues
import android.provider.MediaStore
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.R
import dev.tcode.thinmp.activity.HiltTestActivity
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * お気に入りの一覧で曲を外すと、その行が一覧から消える。
 * 書き込みより先に読み直すと、外した行が残る。
 *
 * ViewModelは自分でリポジトリを作るので、アプリのデータベースを使い、テストの前後でお気に入りを消す。
 *
 * 曲はテストがMediaStoreに挿入する。
 * 端末の曲を使うと、前のテストの再生が残っているときにミニプレーヤーにも同じ曲名が出て、行を見分けられない。
 */
@RunWith(AndroidJUnit4::class)
class FavoriteSongsRemoveTest {
    private val timeoutMs = 10_000L
    private val songName = "thinmp_test_remove_song"

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var application: Application

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(application.packageName, "android.permission.READ_MEDIA_AUDIO")

        deleteSong()
        insertSong()

        val song = SongRepository(application).findAll().first { it.name == songName }

        FavoriteSongRepository().replaceAll(listOf(song.songId))
    }

    @After
    fun tearDown() = runBlocking {
        FavoriteSongRepository().replaceAll(emptyList())
        deleteSong()
    }

    @Test
    fun removingASongTakesItsRowOffTheList() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides NoNavigator) {
                FavoriteSongsScreen()
            }
        }

        awaitNodes(songName, present = true)
        composeTestRule.onNodeWithText(songName).performTouchInput { longClick() }

        // 状態を読み終えるまで、項目は無効のまま「追加」と表示される。
        val remove = application.getString(R.string.remove_favorite)

        awaitNodes(remove, present = true)
        composeTestRule.onNodeWithText(remove).performClick()

        awaitNodes(songName, present = false)
    }

    private fun insertSong() {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$songName.mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.TITLE, songName)
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
        }

        application.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
    }

    /**
     * 前回の実行がtearDownの前に止まっていても影響しないよう、挿入の前にも呼ぶ。
     */
    private fun deleteSong() {
        application.contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} = ?", arrayOf(songName))
    }

    private fun awaitNodes(text: String, present: Boolean) {
        composeTestRule.waitUntil(timeoutMs) { composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() == present }
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
