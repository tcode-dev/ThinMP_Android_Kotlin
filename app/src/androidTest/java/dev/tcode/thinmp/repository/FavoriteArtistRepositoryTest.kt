package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.room.FavoriteArtistEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteArtistRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: FavoriteArtistRepository

    @Before
    fun setUp() {
        db = createTestDatabase()
        repository = FavoriteArtistRepository(db.favoriteArtistDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addAndDeleteAreVisibleToExists() = runTest {
        assertFalse(repository.exists(ArtistId("1")))

        repository.add(ArtistId("1"))
        assertTrue(repository.exists(ArtistId("1")))

        repository.delete(ArtistId("1"))
        assertFalse(repository.exists(ArtistId("1")))
    }

    @Test
    fun replaceAllKeepsTheGivenOrder() = runTest {
        repository.add(ArtistId("1"))

        repository.replaceAll(listOf(ArtistId("3"), ArtistId("2")))

        assertEquals(listOf(ArtistId("3"), ArtistId("2")), repository.findAll())
    }

    @Test
    fun replaceAllRollsBackWhenAnInsertFails() = runTest {
        val dao = db.favoriteArtistDao()
        repository.replaceAll(listOf(ArtistId("1"), ArtistId("2"), ArtistId("3")))

        val collidingId = "duplicate-primary-key"
        val entities = listOf(
            FavoriteArtistEntity(id = collidingId, artistId = "4"), FavoriteArtistEntity(id = collidingId, artistId = "5")
        )

        try {
            dao.replaceAll(entities)
            throw AssertionError("expected the duplicate primary key to abort the insert")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // The transaction must roll back the deleteAll() that preceded the failing insert.
        }

        assertEquals(listOf(ArtistId("1"), ArtistId("2"), ArtistId("3")), repository.findAll())
    }
}
