package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Modifier
import kotlin.coroutines.Continuation

/**
 * Roomに.allowMainThreadQueries()が不要であることを確かめる。
 * 呼び出しのテストは一部のDAO関数しか通らないので、すべてのDAO関数がsuspendであることはリフレクションで確かめる。
 */
@RunWith(AndroidJUnit4::class)
class MainThreadAccessTest {
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = createTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * DAOはAppDatabaseの抽象メソッドから集めるので、DAOを足しても一覧を直す必要がない。
     * Roomの@Daoは実行時に残らないので、注釈ではなく抽象メソッドの戻り値で見分ける。
     * suspend関数はJVMでは最後の引数がContinuationになる。
     */
    @Test
    fun everyDaoFunctionIsSuspend() {
        val daos = AppDatabase::class.java.declaredMethods
            .filter { Modifier.isAbstract(it.modifiers) }
            .map { it.returnType }

        assertTrue(daos.isNotEmpty())

        val blocking = daos.flatMap { dao ->
            dao.declaredMethods
                // コンパイラーが生成する静的なメソッドは、DAOの関数ではない
                .filterNot { it.isSynthetic || Modifier.isStatic(it.modifiers) }
                .filterNot { it.parameterTypes.lastOrNull() == Continuation::class.java }
                .map { "${dao.simpleName}.${it.name}" }
        }

        assertEquals(emptyList<String>(), blocking)
    }

    @Test
    fun favouritesCanBeReadAndWrittenFromTheMainThread() = runBlocking(Dispatchers.Main) {
        val songs = FavoriteSongRepository(db.favoriteSongDao())
        val artists = FavoriteArtistRepository(db.favoriteArtistDao())

        songs.add(SongId("1"))
        artists.add(ArtistId("1"))

        assertTrue(songs.exists(SongId("1")))
        assertTrue(artists.exists(ArtistId("1")))
    }

    @Test
    fun playlistsAndShortcutsCanBeReadAndWrittenFromTheMainThread() = runBlocking(Dispatchers.Main) {
        val playlists = PlaylistRepository(db)
        val shortcuts = ShortcutRepository(db)

        playlists.create(SongId("1"), "first")
        shortcuts.add(ArtistId("1"))

        assertTrue(playlists.findAll().isNotEmpty())
        assertTrue(shortcuts.findAll().isNotEmpty())
    }
}
