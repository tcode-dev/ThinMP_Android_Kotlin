package dev.tcode.thinmp.viewModel

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.FavoriteArtistRepository
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.ShortcutRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 一覧はonToggledで読み直すので、呼ばれた時点で書き込みが終わっていなければならない。
 * 終わる前に読み直すと、外した行が一覧に残る。
 *
 * ViewModelは自分でリポジトリを作るので、インメモリではなくアプリのデータベースを使う。
 */
@RunWith(AndroidJUnit4::class)
class RegisterToggleCompletionTest {
    private val timeoutSeconds = 10L
    private val songId = SongId("thinmp_test_toggle_song")
    private val artistId = ArtistId("thinmp_test_toggle_artist")

    @Before
    fun setUp() = runBlocking {
        clear()
    }

    @After
    fun tearDown() = runBlocking {
        clear()
    }

    @Test
    fun aFavoriteSongIsWrittenBeforeOnToggled() {
        val viewModel = onMain { FavoriteSongRegisterViewModel() }

        assertWrittenWhenToggled({ viewModel.toggle(songId, it) }, { viewModel.isFavorite(songId) })
    }

    @Test
    fun aFavoriteArtistIsWrittenBeforeOnToggled() {
        val viewModel = onMain { FavoriteArtistRegisterViewModel() }

        assertWrittenWhenToggled({ viewModel.toggle(artistId, it) }, { viewModel.isFavorite(artistId) })
    }

    @Test
    fun aShortcutIsWrittenBeforeOnToggled() {
        val viewModel = onMain { ShortcutViewModel() }

        assertWrittenWhenToggled({ viewModel.toggle(artistId, it) }, { viewModel.isShortcut(artistId) })
    }

    /**
     * 待っている間に書き込みが終わって通ることがないよう、onToggledの中で読む。
     */
    private fun assertWrittenWhenToggled(toggle: (() -> Unit) -> Unit, isWritten: suspend () -> Boolean) {
        val toggled = CountDownLatch(1)
        var written = false

        onMain {
            toggle {
                written = runBlocking { isWritten() }
                toggled.countDown()
            }
        }

        assertTrue("onToggled was never called", toggled.await(timeoutSeconds, TimeUnit.SECONDS))
        assertTrue("onToggled ran before the write finished", written)
    }

    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private suspend fun clear() {
        FavoriteSongRepository().delete(songId)
        FavoriteArtistRepository().delete(artistId)

        val shortcuts = ShortcutRepository()

        if (shortcuts.exists(artistId)) shortcuts.toggle(artistId)
    }
}
