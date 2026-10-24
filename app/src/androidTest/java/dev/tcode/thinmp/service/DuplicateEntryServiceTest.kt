package dev.tcode.thinmp.service

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.FavoriteSongEntity
import dev.tcode.thinmp.repository.AppDatabase
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.createTestDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * これらのサービスは、idの件数とMediaStoreの行数が一致するまで再取得するのではなく、
 * 変換済みのリストをそのまま返す。
 * idが重複していると(SQLのINは重複をまとめるので)件数が一致せず、再取得のループは終わらない。
 * 重複はスキーマでも禁止している。
 *
 * 端末に音声ファイルが必要で、なければスキップする。
 */
@RunWith(AndroidJUnit4::class)
class DuplicateEntryServiceTest {
    private val timeoutMs = 10_000L

    private lateinit var db: AppDatabase
    private lateinit var context: Context
    private var realSongIdValue: String = ""
    private val realSongId: SongId get() = SongId(realSongIdValue)

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        db = createTestDatabase()

        val songs = SongRepository(context).findAll()
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        realSongIdValue = songs.first().songId.id
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** (playlist_id, song_id)が主キーなので、同じ曲の2回目のaddは無視され、重複行はできない。 */
    @Test
    fun duplicatePlaylistSongRowCannotBeCreated() = runBlocking {
        val repository = PlaylistRepository(db)
        repository.create(realSongId, "duplicate")
        val playlistId = PlaylistId(repository.findAll().first().id)

        repository.add(playlistId, realSongId)

        assertEquals(1, repository.findSongsByPlaylistId(playlistId).size)

        val playlist = withTimeout(timeoutMs) { PlaylistDetailService(context, repository).findById(playlistId) }

        assertEquals(1, playlist?.songs?.size)
    }

    /** お気に入りも同じく、songIdが主キーなので重複行はできない。 */
    @Test
    fun duplicateFavouriteRowCannotBeCreated() = runBlocking {
        db.favoriteSongDao().insert(FavoriteSongEntity(songId = realSongIdValue))

        try {
            db.favoriteSongDao().insert(FavoriteSongEntity(songId = realSongIdValue))
            throw AssertionError("expected the primary key to reject the duplicate favourite")
        } catch (expected: SQLiteConstraintException) {
            // 2行目は書き込まれていないので、片付けるものはない。
        }

        val songs = withTimeout(timeoutMs) {
            FavoriteSongsService(context, FavoriteSongRepository(db.favoriteSongDao())).findAll()
        }

        assertEquals(1, songs.size)
    }

    /** どの曲にも解決しないidは削除される。 */
    @Test
    fun favouriteWhoseFileIsGoneIsDropped() = runBlocking {
        val repository = FavoriteSongRepository(db.favoriteSongDao())
        repository.add(realSongId)
        repository.add(SongId("-1"))

        val songs = withTimeout(timeoutMs) { FavoriteSongsService(context, repository).findAll() }

        assertEquals(1, songs.size)
        assertTrue(repository.findAll().none { it.id == "-1" })
    }
}
