package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.assertHasAudio
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.FavoriteArtistRepository
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.ShortcutRepository
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 完了ボタンは最初のフレームから表示される(EditTopAppBarViewがアニメーションするのはバーの背景だけ)が、
 * 一覧は非同期に読み込まれる。
 * save()はその時点のuiStateを保存し、初期状態は空の一覧なので、
 * 読み込み前にタップすると空の一覧を保存してしまう。
 * reorder()もreplaceAll()も一覧にないものは削除する(スワイプで空にした一覧もこうして保存する)ので、
 * テーブルの中身がすべて消える。
 *
 * ボタンは読み込みが終わるまで無効にし、save()自身も読み込み前は何もしない。
 * このテストは後者を、すり抜けたタップと同じくsave()を直接呼んで確かめる。
 *
 * コンストラクター直後にsave()を呼んでも、読み込みより先になるとは限らない。
 * withContextは処理がすぐ終われば中断せずに戻るので、データベースが温まっていてテーブルが小さいと、
 * 読み込みがコンストラクターの中で終わることがある。
 * そのためRoomだけを読む画面のテストは、save()を呼んだ時点で読み込みが終わっていたかを記録し、
 * 終わっていれば保存の完了を、終わっていなければ保存しなかったことを待ってからデータベースを確かめる。
 * ガード自体は、終わっても状態を埋めない読み込みを使うsavingWithoutLoadedStateReportsNothingで、
 * 競争に頼らずに確かめる。
 *
 * MediaStoreも読む画面は、Dispatchers.IOに切り替えてから戻るので、コンストラクターと同じメインスレッドの処理の中では
 * 読み込みが終わらない。
 * そのため、読み込み前に保存しないことを順序に頼らずに確かめられる。
 *
 * これらのview modelは自分でリポジトリを作るので、インメモリではなく端末のアプリのデータベースを使う。
 * 各テストは触るデータを前後で消す。
 */
@RunWith(AndroidJUnit4::class)
class EditSaveBeforeLoadTest {
    private val timeoutMs = 10_000L
    private val quietMs = 1_000L

    private lateinit var application: Application
    private var songIdValue: String = ""
    private var artistIdValue: String = ""

    @Before
    fun setUp() = runBlocking {
        application = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(application.packageName, "android.permission.READ_MEDIA_AUDIO")
        clear()

        val songs = SongRepository(application).findAll()
        assertHasAudio(songs.size)
        songIdValue = songs.first().songId.id
        artistIdValue = songs.first().artistId.id
    }

    @After
    fun tearDown() = runBlocking {
        clear()
    }

    @Test
    fun savingBeforeTheListLoadsKeepsThePlaylists() = runBlocking {
        val repository = PlaylistRepository()
        repository.create(SongId(songIdValue), "first")
        repository.create(SongId(songIdValue), "second")

        val (viewModel, loadedAtSave) = onMain { PlaylistsEditViewModel(application).let { it to saveRightAway(it::save) { it.uiState.value.loaded } } }
        awaitLoaded { viewModel.uiState.value.loaded }
        awaitSaveOutcome(loadedAtSave, viewModel.saved)

        assertEquals(listOf("first", "second"), repository.findAll().map { it.name })
    }

    @Test
    fun savingBeforeTheListLoadsKeepsTheFavourites() = runBlocking {
        val repository = FavoriteSongRepository()
        repository.add(SongId(songIdValue))

        val (viewModel, loadedAtSave) = onMain {
            FavoriteSongsEditViewModel(application, MediaStoreFailure()).let { it to saveRightAway(it::save) { it.uiState.value.loaded } }
        }
        awaitLoaded { viewModel.uiState.value.loaded }
        awaitSaveOutcome(loadedAtSave, viewModel.saved)

        assertEquals(listOf(songIdValue), repository.findAll().map { it.id })
    }

    /**
     * 読み込み前に保存すると、ショートカットが空の一覧で並べ直されてすべて消える。
     */
    @Test
    fun savingBeforeTheMainMenuLoadsReportsNothing() = runBlocking {
        val repository = ShortcutRepository()
        repository.add(ArtistId(artistIdValue))

        val (viewModel, loadedAtSave) = onMain {
            MainEditViewModel(application, MediaStoreFailure()).let { it to saveRightAway(it::save) { it.uiState.value.loaded } }
        }

        assertFalse(loadedAtSave)

        awaitLoaded { viewModel.uiState.value.loaded }
        awaitSaveOutcome(loadedAtSave, viewModel.saved)

        assertEquals(listOf(artistIdValue), repository.findAll().map { it.itemId })
    }

    @Test
    fun savingBeforeTheFavouriteArtistsLoadReportsNothing() = runBlocking {
        val repository = FavoriteArtistRepository()
        repository.add(ArtistId(artistIdValue))

        val (viewModel, loadedAtSave) = onMain {
            FavoriteArtistsEditViewModel(application, MediaStoreFailure()).let { it to saveRightAway(it::save) { it.uiState.value.loaded } }
        }

        assertFalse(loadedAtSave)

        awaitLoaded { viewModel.uiState.value.loaded }
        awaitSaveOutcome(loadedAtSave, viewModel.saved)

        assertEquals(listOf(ArtistId(artistIdValue)), repository.findAll())
    }

    /**
     * 読み込み前のタップは何もしない。
     * 保存済みと通知すると、何も保存していないのに前の画面に戻り、保存されたように見えてしまう。
     *
     * 存在しないプレイリストidでは、findByIdがnullを返してload()が途中で戻るので、
     * どれだけ待っても`loaded`はfalseのまま。
     * ガードが必要な状態をそのまま保てる。
     */
    @Test
    fun savingWithoutLoadedStateReportsNothing() = runBlocking {
        val viewModel = onMain { PlaylistDetailEditViewModel(application, SavedStateHandle(mapOf("id" to "gone")), MediaStoreFailure()) }

        delay(quietMs)
        assertFalse(viewModel.uiState.value.loaded)

        onMain { viewModel.save() }

        assertNull(withTimeoutOrNull(quietMs) { viewModel.saved.flow.first() })
    }

    /**
     * 読み込み後の保存では、画面の一覧が保存される。
     */
    @Test
    fun savingAfterTheListLoadsAppliesTheList() = runBlocking {
        val repository = PlaylistRepository()
        repository.create(SongId(songIdValue), "first")
        repository.create(SongId(songIdValue), "second")

        val viewModel = onMain { PlaylistsEditViewModel(application) }
        awaitLoaded { viewModel.uiState.value.loaded }
        onMain { viewModel.removePlaylist(viewModel.uiState.value.playlists.first().id) }
        onMain { viewModel.save() }
        withTimeout(timeoutMs) { viewModel.saved.flow.first() }

        assertEquals(listOf("second"), repository.findAll().map { it.name })
    }

    /**
     * 保存が終わった後にもう一度タップしても、保存済みは1回しか通知しない。
     * 2回通知すると、画面を閉じる遷移の途中でbackFromEvent()がもう一度呼ばれ、前の画面まで閉じてしまう。
     */
    @Test
    fun savingTwiceReportsOnce() = runBlocking {
        val repository = PlaylistRepository()
        repository.create(SongId(songIdValue), "first")

        val viewModel = onMain { PlaylistsEditViewModel(application) }
        awaitLoaded { viewModel.uiState.value.loaded }
        onMain { viewModel.save() }
        withTimeout(timeoutMs) { viewModel.saved.flow.first() }
        onMain { viewModel.save() }

        assertNull(withTimeoutOrNull(quietMs) { viewModel.saved.flow.first() })
    }

    /**
     * スワイプで空にした一覧も保存できる。
     * すべて消せなければならない。
     */
    @Test
    fun savingAnEmptiedListDeletesEverything() = runBlocking {
        val repository = PlaylistRepository()
        repository.create(SongId(songIdValue), "first")

        val viewModel = onMain { PlaylistsEditViewModel(application) }
        awaitLoaded { viewModel.uiState.value.loaded }
        onMain { viewModel.removePlaylist(viewModel.uiState.value.playlists.first().id) }
        onMain { viewModel.save() }
        withTimeout(timeoutMs) { viewModel.saved.flow.first() }

        assertEquals(emptyList<String>(), repository.findAll().map { it.name })
    }

    /**
     * save()を呼び、その時点で読み込みが終わっていたかを返す。
     * どちらもメインスレッドで続けて行うので、save()が見たのと同じ状態になる。
     */
    private fun saveRightAway(save: () -> Unit, loaded: () -> Boolean): Boolean {
        save()

        return loaded()
    }

    /**
     * 読み込みの後に保存したなら、書き込みが終わるまで待つ。
     * 読み込みの前なら、保存しなかったこと(savedが届かないこと)を確かめる。
     * OneShotEventは受け取られるまで値を残すので、先に届いていても取りこぼさない。
     */
    private suspend fun awaitSaveOutcome(loadedAtSave: Boolean, saved: OneShotEvent<Unit>) {
        if (loadedAtSave) {
            withTimeout(timeoutMs) { saved.flow.first() }
        } else {
            assertNull(withTimeoutOrNull(quietMs) { saved.flow.first() })
        }
    }

    private suspend fun awaitLoaded(loaded: () -> Boolean) {
        withTimeout(timeoutMs) {
            while (!loaded()) {
                kotlinx.coroutines.delay(20)
            }
        }
    }

    /**
     * view modelの操作はすべてメインスレッドで行う。
     * viewModelScopeはDispatchers.Main.immediateなので、他のスレッドから作ると処理がpostされ、
     * load()が始まる前にコンストラクターが戻ってしまう。
     */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        return result!!
    }

    private suspend fun clear() {
        val playlists = PlaylistRepository()

        playlists.findAll().forEach { playlists.delete(PlaylistId(it.id)) }

        val favourites = FavoriteSongRepository()

        favourites.deleteByIds(favourites.findAll())

        val favouriteArtists = FavoriteArtistRepository()

        favouriteArtists.deleteByIds(favouriteArtists.findAll())

        val shortcuts = ShortcutRepository()

        shortcuts.deleteByIds(shortcuts.findAll().map { it.id })
    }
}
