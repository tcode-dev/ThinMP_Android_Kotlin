package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression guard for removing .allowMainThreadQueries(). The database here is built without it,
 * so if any DAO function stops being suspend, Room's assertNotMainThread() fires and these fail.
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
