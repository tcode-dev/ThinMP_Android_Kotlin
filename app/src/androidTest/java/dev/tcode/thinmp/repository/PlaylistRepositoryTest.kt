package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaylistRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: PlaylistRepository

    @Before
    fun setUp() {
        db = createTestDatabase()
        repository = PlaylistRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createNumbersPlaylistsFromOne() = runTest {
        repository.create(SongId("1"), "first")
        repository.create(SongId("2"), "second")

        val playlists = repository.findAll()

        assertEquals(listOf("first", "second"), playlists.map { it.name })
        assertEquals(listOf(1, 2), playlists.map { it.order })
    }

    @Test
    fun createAddsTheSeedSong() = runTest {
        repository.create(SongId("1"), "first")

        val playlist = repository.findAll().first()

        assertEquals(listOf("1"), repository.findSongsByPlaylistId(PlaylistId(playlist.id)).map { it.songId })
    }

    @Test
    fun updatePlaylistReplacesNameAndSongs() = runTest {
        repository.create(SongId("1"), "first")
        val id = PlaylistId(repository.findAll().first().id)

        repository.updatePlaylist(id, "renamed", listOf(SongId("2"), SongId("3")))

        assertEquals("renamed", repository.findById(id)?.name)
        assertEquals(listOf("2", "3"), repository.findSongsByPlaylistId(id).map { it.songId })
    }

    @Test
    fun deleteRemovesThePlaylistAndItsSongs() = runTest {
        repository.create(SongId("1"), "first")
        val id = PlaylistId(repository.findAll().first().id)

        repository.delete(id)

        assertNull(repository.findById(id))
        assertTrue(repository.findSongsByPlaylistId(id).isEmpty())
    }

    @Test
    fun reorderRenumbersSurvivorsAndDropsTheRest() = runTest {
        repository.create(SongId("1"), "first")
        repository.create(SongId("2"), "second")
        repository.create(SongId("3"), "third")
        val ids = repository.findAll().map { PlaylistId(it.id) }

        // Keep the third and the first, in that order; the second is dropped.
        repository.reorder(listOf(ids[2], ids[0]))

        val playlists = repository.findAll()

        assertEquals(listOf("third", "first"), playlists.map { it.name })
        assertEquals(listOf(1, 2), playlists.map { it.order })
        assertTrue(repository.findSongsByPlaylistId(ids[1]).isEmpty())
    }
}
