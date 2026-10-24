package dev.tcode.thinmp.repository

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.ShortcutEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TITLE_PREFIX = "thinmp_test_limit_"

/**
 * この端末のSQLiteで`IN (?,?,...)`に使える上限32766を1つ超える数。
 * 分割せずに送ると、どのクエリも`too many SQL variables`で失敗する。
 * 3.32より古いSQLiteの上限は999なので、
 * SqliteConstant.MAX_VARIABLESはこのエミュレーターの値ではなく999にしている。
 */
private const val OVER_LIMIT = 32767

/**
 * idのリストはユーザーのデータなので、長さに上限はない(お気に入り1000件、全曲入りのプレイリストなど)。
 * 長いリストの先頭・中央・末尾に実在するidを置くので、
 * 最初や最後のチャンクしか実行しない分割も検出できる。
 */
@RunWith(AndroidJUnit4::class)
class SqliteVariableLimitTest {
    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        db = createTestDatabase()
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        deleteTracks()
    }

    @After
    fun tearDown() {
        deleteTracks()
        db.close()
    }

    @Test
    fun favoriteSongsCanBeDeletedByMoreIdsThanSqliteBinds() = runTest {
        val repository = FavoriteSongRepository(db.favoriteSongDao())
        val present = listOf(SongId("first"), SongId("middle"), SongId("last"))
        repository.replaceAll(present)

        repository.deleteByIds(spread(present.map { it.id }).map { SongId(it) })

        assertTrue(repository.findAll().isEmpty())
    }

    @Test
    fun favoriteArtistsCanBeDeletedByMoreIdsThanSqliteBinds() = runTest {
        val repository = FavoriteArtistRepository(db.favoriteArtistDao())
        val present = listOf(ArtistId("first"), ArtistId("middle"), ArtistId("last"))
        repository.replaceAll(present)

        repository.deleteByIds(spread(present.map { it.id }).map { ArtistId(it) })

        assertTrue(repository.findAll().isEmpty())
    }

    @Test
    fun playlistsCanBeFoundByMoreIdsThanSqliteBinds() = runTest {
        val repository = PlaylistRepository(db)
        repository.create(SongId("1"), "first")
        repository.create(SongId("2"), "second")
        repository.create(SongId("3"), "third")
        val ids = repository.findAll().map { it.id }

        val found = repository.findByIds(spread(ids).map { PlaylistId(it) })

        assertEquals(setOf("first", "second", "third"), found.map { it.name }.toSet())
    }

    @Test
    fun playlistsCanBeDeletedByMoreIdsThanSqliteBinds() = runTest {
        val repository = PlaylistRepository(db)
        repository.create(SongId("1"), "first")
        repository.create(SongId("2"), "second")
        repository.create(SongId("3"), "third")
        val ids = repository.findAll().map { it.id }

        db.playlistDao().deleteByIds(spread(ids))

        assertTrue(repository.findAll().isEmpty())
    }

    @Test
    fun playlistSongsCanBeDeletedByMoreIdsThanSqliteBinds() = runTest {
        val repository = PlaylistRepository(db)
        repository.create(SongId("first"), "playlist")
        val playlistId = PlaylistId(repository.findAll().first().id)
        repository.add(playlistId, SongId("middle"))
        repository.add(playlistId, SongId("last"))

        repository.delete(playlistId, spread(listOf("first", "middle", "last")).map { SongId(it) })

        assertTrue(repository.findSongsByPlaylistId(playlistId).isEmpty())
    }

    @Test
    fun shortcutsCanBeDeletedByMoreIdsThanSqliteBinds() = runTest {
        val dao = db.shortcutDao()
        dao.insert(ShortcutEntity(itemId = "1", type = 0, order = 1))
        dao.insert(ShortcutEntity(itemId = "2", type = 0, order = 2))
        dao.insert(ShortcutEntity(itemId = "3", type = 0, order = 3))
        val ids = dao.findAll().map { it.id }

        dao.deleteByIds(spread(ids))

        assertTrue(dao.findAll().isEmpty())
    }

    /**
     * MediaStore側の同じ問題。
     * こちらの文はMediaProviderが自分のプロセスのSQLiteでコンパイルするので、
     * アプリから上限を知る方法がない。
     */
    @Test
    fun songsCanBeFoundByMoreIdsThanSqliteBinds() = runBlocking {
        val songs = insertTracks()

        val found = SongRepository(context).findByIds(spread(songs.map { it.songId.id }).map { SongId(it) })

        assertEquals(songs.map { it.name }.toSet(), found.map { it.name }.toSet())
    }

    @Test
    fun albumsCanBeFoundByMoreIdsThanSqliteBinds() = runBlocking {
        val songs = insertTracks()

        val found = AlbumRepository(context).findByIds(spread(songs.map { it.albumId.id }).map { AlbumId(it) })

        assertEquals(songs.map { it.albumName }.toSet(), found.map { it.name }.toSet())
    }

    @Test
    fun artistsCanBeFoundByMoreIdsThanSqliteBinds() = runBlocking {
        val songs = insertTracks()

        val found = ArtistRepository(context).findByIds(spread(songs.map { it.artistId.id }).map { ArtistId(it) })

        assertEquals(songs.map { it.artistName }.toSet(), found.map { it.name }.toSet())
    }

    /** アルバムもアーティストも別々の3曲。1回の挿入で3つのテストに使える。 */
    private suspend fun insertTracks(): List<SongModel> {
        listOf("a", "b", "c").forEach { name ->
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "$TITLE_PREFIX$name.mp3")
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.TITLE, "$TITLE_PREFIX$name")
                put(MediaStore.Audio.Media.ARTIST, "${TITLE_PREFIX}artist_$name")
                put(MediaStore.Audio.Media.ALBUM, "${TITLE_PREFIX}album_$name")
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
            }

            context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        }

        return SongRepository(context).findAll().filter { it.name.startsWith(TITLE_PREFIX) }
    }

    /** 前回の実行がtearDownの前に止まっていても影響しないよう、挿入の前にも呼ぶ。 */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }

    /**
     * どれにも一致しない[OVER_LIMIT]個のidのリストの、先頭・中央・末尾に[ids]を置いて返す。
     * 数で上限を超えさせ、置く位置で実行されなかったチャンクを検出する。
     */
    private fun spread(ids: List<String>): List<String> {
        require(ids.size == 3) { "spread() places exactly three ids" }

        val list = (1..OVER_LIMIT).map { "absent_$it" }.toMutableList()
        list[0] = ids[0]
        list[list.size / 2] = ids[1]
        list[list.size - 1] = ids[2]

        return list
    }
}
