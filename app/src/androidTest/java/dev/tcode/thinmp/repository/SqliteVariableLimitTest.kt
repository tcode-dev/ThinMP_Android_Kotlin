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
 * One over the 32766 the SQLite on this device compiles `IN (?,?,...)` with. It is the number at
 * which every query below threw `too many SQL variables` before the lists were chunked, so it is
 * what makes these tests fail when the chunking is taken back out. Devices whose SQLite predates
 * 3.32 hit the same wall at 1000, which is why SqliteConstant.MAX_VARIABLES cuts at 999 rather
 * than at what this emulator happens to allow.
 */
private const val OVER_LIMIT = 32767

/**
 * The id list is the user's data, so its length is theirs to decide: 1000 favourites, a playlist
 * of everything on the device. Each test puts a real id at the start, the middle and the end of an
 * over-long list, so a chunking that stopped after the first chunk - or kept only the last - fails
 * as loudly as no chunking at all.
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
     * The MediaStore half of the same problem, and the one the app cannot size up in advance: these
     * statements are compiled by MediaProvider, in its own process and with its own SQLite.
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

    /** Three tracks, each its own album and its own artist, so one insert serves all three tests. */
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

    /** Also runs before the inserts: a run that died before its tearDown must not skew the next. */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }

    /**
     * [ids] placed at the first, middle and last position of a list of [OVER_LIMIT] ids that match
     * nothing. The padding is what puts the statement over the limit; the placement is what makes
     * the assertion notice a chunk that never ran.
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
