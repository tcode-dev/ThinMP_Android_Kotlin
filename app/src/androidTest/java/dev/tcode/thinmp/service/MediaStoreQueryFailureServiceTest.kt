package dev.tcode.thinmp.service

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.AlbumRepository
import dev.tcode.thinmp.repository.AppDatabase
import dev.tcode.thinmp.repository.ArtistRepository
import dev.tcode.thinmp.repository.FavoriteArtistRepository
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.MediaStoreQueryException
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.ShortcutRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.createTestDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MediaStoreの問い合わせが失敗したら、RoomのidをMediaStoreと突き合わせるサービスは例外で止まり、登録を消さない。
 * 失敗を空の一覧として扱うと、すべてのidが「MediaStoreから消えた」ことになり、全件削除してしまう。
 *
 * ContentResolver.query()はfinalで上書きできないので、存在しないauthorityに問い合わせて失敗させる。
 * プロバイダーが見つからないとquery()はnullを返す。
 *
 * idはMediaStoreにない値なので、端末に音声ファイルは要らない。
 * 問い合わせが成功すれば消えるidなので、成功したときに消えることも確かめ、
 * 失敗のテストが削除の起きない状態で通っているだけではないことを示す。
 */
@RunWith(AndroidJUnit4::class)
class MediaStoreQueryFailureServiceTest {
    private val missingUri: Uri = Uri.parse("content://dev.tcode.thinmp.test.missing")
    private val songId = SongId("thinmp_test_missing_song")
    private val artistId = ArtistId("thinmp_test_missing_artist")
    private val albumId = AlbumId("thinmp_test_missing_album")

    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        db = createTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun favoriteSongsAreKeptWhenTheQueryFails() = runBlocking {
        val favorites = FavoriteSongRepository(db.favoriteSongDao())
        favorites.add(songId)

        assertThrows(MediaStoreQueryException::class.java) {
            runBlocking { FavoriteSongsService(context, favorites, SongRepository(context, missingUri)).findAll() }
        }
        assertEquals(listOf(songId), favorites.findAll())
    }

    @Test
    fun favoriteSongsAreRemovedWhenTheQuerySucceeds() = runBlocking {
        val favorites = FavoriteSongRepository(db.favoriteSongDao())
        favorites.add(songId)

        FavoriteSongsService(context, favorites, SongRepository(context)).findAll()

        assertEquals(emptyList<SongId>(), favorites.findAll())
    }

    @Test
    fun favoriteArtistsAreKeptWhenTheQueryFails() = runBlocking {
        val favorites = FavoriteArtistRepository(db.favoriteArtistDao())
        favorites.add(artistId)

        assertThrows(MediaStoreQueryException::class.java) {
            runBlocking { FavoriteArtistsService(context, favorites, ArtistRepository(context, missingUri)).findAll() }
        }
        assertEquals(listOf(artistId), favorites.findAll())
    }

    @Test
    fun favoriteArtistsAreRemovedWhenTheQuerySucceeds() = runBlocking {
        val favorites = FavoriteArtistRepository(db.favoriteArtistDao())
        favorites.add(artistId)

        FavoriteArtistsService(context, favorites, ArtistRepository(context)).findAll()

        assertEquals(emptyList<ArtistId>(), favorites.findAll())
    }

    @Test
    fun playlistSongsAreKeptWhenTheQueryFails() = runBlocking {
        val playlists = PlaylistRepository(db)
        val playlistId = createPlaylist(playlists)

        assertThrows(MediaStoreQueryException::class.java) {
            runBlocking { PlaylistDetailService(context, playlists, SongRepository(context, missingUri)).findById(playlistId) }
        }
        assertEquals(listOf(songId.id), playlists.findSongsByPlaylistId(playlistId).map { it.songId })
    }

    @Test
    fun playlistSongsAreRemovedWhenTheQuerySucceeds() = runBlocking {
        val playlists = PlaylistRepository(db)
        val playlistId = createPlaylist(playlists)

        PlaylistDetailService(context, playlists, SongRepository(context)).findById(playlistId)

        assertEquals(emptyList<String>(), playlists.findSongsByPlaylistId(playlistId).map { it.songId })
    }

    /**
     * 種類ごとに問い合わせる経路が違うので、1種類ずつ確かめる。
     * プレイリストはRoomにあり、画像のために先頭の曲をMediaStoreに問い合わせる。
     */
    @Test
    fun shortcutsAreKeptWhenTheQueryFails() = runBlocking {
        val shortcuts = ShortcutRepository(db)
        val playlists = PlaylistRepository(db)
        val playlistId = createPlaylist(playlists)

        listOf(artistId, albumId, playlistId).forEach { itemId ->
            shortcuts.add(itemId)

            assertThrows(MediaStoreQueryException::class.java) {
                runBlocking { failingShortcutService(shortcuts, playlists).findAll() }
            }
            assertEquals(1, shortcuts.findAll().size)

            shortcuts.delete(itemId)
        }
    }

    @Test
    fun shortcutsAreRemovedWhenTheQuerySucceeds() = runBlocking {
        val shortcuts = ShortcutRepository(db)
        val playlists = PlaylistRepository(db)
        shortcuts.add(artistId)
        shortcuts.add(albumId)

        ShortcutService(context, context.resources, shortcuts, ArtistRepository(context), AlbumRepository(context), SongRepository(context), playlists).findAll()

        assertEquals(0, shortcuts.findAll().size)
    }

    private suspend fun createPlaylist(playlists: PlaylistRepository): PlaylistId {
        playlists.create(songId, "thinmp_test_playlist")

        return PlaylistId(playlists.findAll().first().id)
    }

    private fun failingShortcutService(shortcuts: ShortcutRepository, playlists: PlaylistRepository): ShortcutService {
        return ShortcutService(
            context,
            context.resources,
            shortcuts,
            ArtistRepository(context, missingUri),
            AlbumRepository(context, missingUri),
            SongRepository(context, missingUri),
            playlists
        )
    }
}
