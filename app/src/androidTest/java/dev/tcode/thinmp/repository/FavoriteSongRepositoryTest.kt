package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.FavoriteSongEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteSongRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: FavoriteSongRepository

    @Before
    fun setUp() {
        db = createTestDatabase()
        repository = FavoriteSongRepository(db.favoriteSongDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addAndDeleteAreVisibleToExists() = runTest {
        assertFalse(repository.exists(SongId("1")))

        repository.add(SongId("1"))
        assertTrue(repository.exists(SongId("1")))

        repository.delete(SongId("1"))
        assertFalse(repository.exists(SongId("1")))
    }

    @Test
    fun replaceAllKeepsTheGivenOrder() = runTest {
        repository.add(SongId("1"))

        repository.replaceAll(listOf(SongId("3"), SongId("2")))

        assertEquals(listOf(SongId("3"), SongId("2")), repository.findAll())
    }

    /**
     * The reason replaceAll exists. Before it was a single transaction, the repository ran
     * deleteAll() and then inserted one row at a time; a failure partway through left the user
     * with no favourites at all.
     */
    @Test
    fun replaceAllRollsBackWhenAnInsertFails() = runTest {
        val dao = db.favoriteSongDao()
        repository.replaceAll(listOf(SongId("1"), SongId("2"), SongId("3")))

        val collidingId = "duplicate-primary-key"
        val entities = listOf(
            FavoriteSongEntity(id = collidingId, songId = "4"), FavoriteSongEntity(id = collidingId, songId = "5")
        )

        try {
            dao.replaceAll(entities)
            throw AssertionError("expected the duplicate primary key to abort the insert")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // The transaction must roll back the deleteAll() that preceded the failing insert.
        }

        assertEquals(listOf(SongId("1"), SongId("2"), SongId("3")), repository.findAll())
    }

    @Test
    fun deleteByIdsRemovesOnlyTheGivenIds() = runTest {
        repository.replaceAll(listOf(SongId("1"), SongId("2"), SongId("3")))

        repository.deleteByIds(listOf(SongId("1"), SongId("3")))

        assertEquals(listOf(SongId("2")), repository.findAll())
    }
}
