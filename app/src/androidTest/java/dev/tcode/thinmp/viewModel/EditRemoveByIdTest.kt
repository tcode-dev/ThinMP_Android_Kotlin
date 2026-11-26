package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.assertHasAudio
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 編集画面の行は、位置ではなくidで消す。
 * スワイプの確定までに一覧が入れ替わると、取り込んだ位置は別の行を指すか、範囲外になる。
 * 5つの編集画面は同じ書き方なので、PlaylistsEditViewModelで代表して確かめる。
 *
 * view modelは自分でリポジトリを作るので、端末のアプリのデータベースを使う。
 * 各テストは触るデータを前後で消す。
 */
@RunWith(AndroidJUnit4::class)
class EditRemoveByIdTest {
    private val timeoutMs = 10_000L

    private lateinit var application: Application
    private var songIdValue: String = ""

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(application.packageName, "android.permission.READ_MEDIA_AUDIO")
        clear()

        val songs = SongRepository(application).findAll()
        assertHasAudio(songs.size)
        songIdValue = songs.first().songId.id
    }

    @After
    fun tearDown() = runBlocking {
        clear()
    }

    @Test
    fun removesTheRowWithTheGivenId() = runBlocking {
        val viewModel = loadedViewModel("first", "second", "third")
        val second = viewModel.uiState.value.playlists[1].id

        onMain { viewModel.removePlaylist(second) }

        assertEquals(listOf("first", "third"), viewModel.uiState.value.playlists.map { it.primaryText })
    }

    /**
     * 同じ行のコールバックが2回届いても、2回目は何も消さない。
     * 位置で消すと、2回目は詰めて上がってきた次の行を消してしまう。
     */
    @Test
    fun removingAnIdThatIsAlreadyGoneLeavesTheOtherRows() = runBlocking {
        val viewModel = loadedViewModel("first", "second")
        val first = viewModel.uiState.value.playlists[0].id

        onMain { viewModel.removePlaylist(first) }
        onMain { viewModel.removePlaylist(first) }

        assertEquals(listOf("second"), viewModel.uiState.value.playlists.map { it.primaryText })
    }

    private suspend fun loadedViewModel(vararg names: String): PlaylistsEditViewModel {
        val repository = PlaylistRepository()

        names.forEach { repository.create(SongId(songIdValue), it) }

        val viewModel = onMain { PlaylistsEditViewModel(application) }

        withTimeout(timeoutMs) {
            while (!viewModel.uiState.value.loaded) {
                delay(20)
            }
        }
        assertEquals(names.toList(), viewModel.uiState.value.playlists.map { it.primaryText })

        return viewModel
    }

    /**
     * EditSaveBeforeLoadTest.onMain()を参照。
     */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        return result!!
    }

    private suspend fun clear() {
        val playlists = PlaylistRepository()

        playlists.findAll().forEach { playlists.delete(PlaylistId(it.id)) }
    }
}
