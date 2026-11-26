package dev.tcode.thinmp.service

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.assertHasAudio
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.FavoriteSongEntity
import dev.tcode.thinmp.repository.AppDatabase
import dev.tcode.thinmp.repository.ArtistRepository
import dev.tcode.thinmp.repository.FavoriteArtistRepository
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.ShortcutRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.createTestDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

/**
 * これらのサービスは、idの件数とMediaStoreの行数が一致するまで再取得するのではなく、
 * 変換済みのリストをそのまま返す。
 * idが重複していると(SQLのINは重複をまとめるので)件数が一致せず、再取得のループは終わらない。
 * 重複はスキーマでも禁止している。
 *
 * 重複を作れないので、ループに戻しても終わらなくなることはない。
 * そのため、削除が起きる状態でRoomを何回読んだかをクエリのコールバックで数え、1回であることを確かめる。
 * MediaStoreはidの順に返さないので、Roomの順に並べ直していることも確かめる。
 *
 * 端末に音声ファイルが必要で、なければ失敗する(tools/push-test-audio.sh)。
 */
@RunWith(AndroidJUnit4::class)
class DuplicateEntryServiceTest {
    private val timeoutMs = 10_000L

    private val missingId = "-1"
    private val queries = CopyOnWriteArrayList<String>()

    private lateinit var db: AppDatabase
    private lateinit var context: Context
    private lateinit var songs: List<SongModel>
    private var realSongIdValue: String = ""
    private val realSongId: SongId get() = SongId(realSongIdValue)

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        db = createTestDatabase { queries.add(it) }

        songs = SongRepository(context).findAll()
        assertHasAudio(songs.size)
        realSongIdValue = songs.first().songId.id
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * (playlist_id, song_id)が主キーなので、同じ曲の2回目のaddは無視され、重複行はできない。
     */
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

    /**
     * お気に入りも同じく、songIdが主キーなので重複行はできない。
     */
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

    /**
     * どの曲にも解決しないidは削除され、削除の後に読み直さない。
     */
    @Test
    fun favouriteSongsAreReadOnceEvenWhenOneIsDropped() = runBlocking {
        val repository = FavoriteSongRepository(db.favoriteSongDao())
        repository.add(realSongId)
        repository.add(SongId(missingId))
        queries.clear()

        val found = withTimeout(timeoutMs) { FavoriteSongsService(context, repository).findAll() }

        assertEquals(1, countReads("favorite_songs"))
        assertEquals(listOf(realSongId), found.map { it.songId })
        assertEquals(listOf(realSongId), repository.findAll())
    }

    @Test
    fun favouriteArtistsAreReadOnceEvenWhenOneIsDropped() = runBlocking {
        val artistId = ArtistRepository(context).findAll().first().artistId
        val repository = FavoriteArtistRepository(db.favoriteArtistDao())
        repository.add(artistId)
        repository.add(ArtistId(missingId))
        queries.clear()

        val found = withTimeout(timeoutMs) { FavoriteArtistsService(context, repository).findAll() }

        assertEquals(1, countReads("favorite_artists"))
        assertEquals(listOf(artistId), found.map { it.artistId })
        assertEquals(listOf(artistId), repository.findAll())
    }

    @Test
    fun playlistSongsAreReadOnceEvenWhenOneIsDropped() = runBlocking {
        val repository = PlaylistRepository(db)
        repository.create(realSongId, "missing")
        val playlistId = PlaylistId(repository.findAll().first().id)
        repository.add(playlistId, SongId(missingId))
        queries.clear()

        val playlist = withTimeout(timeoutMs) { PlaylistDetailService(context, repository).findById(playlistId) }

        assertEquals(1, countReads("playlist_songs"))
        assertEquals(listOf(realSongId), playlist?.songs?.map { it.songId })
        assertEquals(listOf(realSongIdValue), repository.findSongsByPlaylistId(playlistId).map { it.songId })
    }

    @Test
    fun shortcutsAreReadOnceEvenWhenOneIsDropped() = runBlocking {
        val artistId = ArtistRepository(context).findAll().first().artistId
        val repository = ShortcutRepository(db)
        repository.add(artistId)
        repository.add(ArtistId(missingId))
        queries.clear()

        val found = withTimeout(timeoutMs) { ShortcutService(context, shortcutRepository = repository, playlistRepository = PlaylistRepository(db)).findAll() }

        assertEquals(1, countReads("shortcuts"))
        assertEquals(listOf(artistId), found.map { it.itemId })
        assertEquals(listOf(artistId.id), repository.findAll().map { it.itemId })
    }

    /**
     * MediaStoreが返す順の逆で登録し、登録した順に返ることを確かめる。
     */
    @Test
    fun favouriteSongsComeBackInTheOrderTheyWereAdded() = runBlocking {
        val added = reverseOfMediaStoreOrder()
        val repository = FavoriteSongRepository(db.favoriteSongDao())
        added.forEach { repository.add(it) }

        val found = withTimeout(timeoutMs) { FavoriteSongsService(context, repository).findAll() }

        assertEquals(added, found.map { it.songId })
    }

    @Test
    fun favouriteArtistsComeBackInTheOrderTheyWereAdded() = runBlocking {
        val artistIds = ArtistRepository(context).findAll().map { it.artistId }
        assumeTrue("needs at least two artists in MediaStore", artistIds.size >= 2)
        val added = ArtistRepository(context).findByIds(artistIds.take(2)).map { it.artistId }.reversed()
        val repository = FavoriteArtistRepository(db.favoriteArtistDao())
        added.forEach { repository.add(it) }

        val found = withTimeout(timeoutMs) { FavoriteArtistsService(context, repository).findAll() }

        assertEquals(added, found.map { it.artistId })
    }

    @Test
    fun playlistSongsComeBackInTheOrderTheyWereAdded() = runBlocking {
        val added = reverseOfMediaStoreOrder()
        val repository = PlaylistRepository(db)
        repository.create(added.first(), "order")
        val playlistId = PlaylistId(repository.findAll().first().id)
        added.drop(1).forEach { repository.add(playlistId, it) }

        val playlist = withTimeout(timeoutMs) { PlaylistDetailService(context, repository).findById(playlistId) }

        assertEquals(added, playlist?.songs?.map { it.songId })
    }

    /**
     * 2曲を、MediaStoreがid指定の問い合わせで返す順の逆に並べたもの。
     * この順で登録すれば、MediaStoreの結果をそのまま返す実装では逆になる。
     */
    private suspend fun reverseOfMediaStoreOrder(): List<SongId> {
        assertHasAudio(songs.size, 2)

        return SongRepository(context).findByIds(songs.take(2).map { it.songId }).map { it.songId }.reversed()
    }

    /**
     * そのテーブルを読んだ回数。
     * 書き込みや、Roomが変更を追うためのクエリは数えない。
     * テスト自身の読み込みも数えるので、結果を確かめる前に呼ぶ。
     */
    private fun countReads(table: String): Int {
        return queries.count { it.startsWith("SELECT * FROM $table") }
    }
}
