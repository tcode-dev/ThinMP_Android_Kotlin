package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.room.FavoriteArtistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
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

        val entities = listOf(FavoriteArtistEntity(artistId = "4"), FavoriteArtistEntity(artistId = "4"))

        try {
            dao.replaceAll(entities)
            throw AssertionError("expected the duplicate primary key to abort the insert")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // 失敗したinsertの前のdeleteAll()はロールバックされていなければならない。
        }

        assertEquals(listOf(ArtistId("1"), ArtistId("2"), ArtistId("3")), repository.findAll())
    }

    @Test
    fun toggleAddsWhenAbsentAndRemovesWhenPresent() = runTest {
        repository.toggle(ArtistId("1"))
        assertTrue(repository.exists(ArtistId("1")))

        repository.toggle(ArtistId("1"))
        assertFalse(repository.exists(ArtistId("1")))
    }

    /**
     * exists()とadd()を別々に呼ぶと間に中断点ができ、
     * 同時に2回toggleすると両方が「未登録」と判断して挿入に進む。
     * 主キーがあるので重複行にはならないがクラッシュする。
     * toggleはそれを防ぐためにある。
     */
    @Test
    fun concurrentTogglesNeverInsertTheSameArtistTwice() = runBlocking {
        repeat(100) {
            db.favoriteArtistDao().deleteAll()

            (1..2).map {
                async(Dispatchers.Default) { repository.toggle(ArtistId("1")) }
            }.awaitAll()

            assertTrue(db.favoriteArtistDao().findAll().size <= 1)
        }
    }

    /** 主キーにより重複行は作れない。 */
    @Test
    fun theSameArtistCannotBeStoredTwice() = runTest {
        val dao = db.favoriteArtistDao()
        dao.insert(FavoriteArtistEntity(artistId = "1"))

        try {
            dao.insert(FavoriteArtistEntity(artistId = "1"))
            throw AssertionError("expected the primary key to reject the duplicate artist")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // 主キーの重複なので失敗するのが正しい。
        }

        assertEquals(listOf(ArtistId("1")), repository.findAll())
    }
}
