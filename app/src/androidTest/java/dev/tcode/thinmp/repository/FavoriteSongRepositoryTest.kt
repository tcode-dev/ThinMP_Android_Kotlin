package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.FavoriteSongEntity
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
     * トランザクションなしでdeleteAll()と1件ずつのinsertを行うと、
     * 途中で失敗したときにお気に入りが全部消える。
     * replaceAllはそれを防ぐためにある。
     */
    @Test
    fun replaceAllRollsBackWhenAnInsertFails() = runTest {
        val dao = db.favoriteSongDao()
        repository.replaceAll(listOf(SongId("1"), SongId("2"), SongId("3")))

        val entities = listOf(FavoriteSongEntity(songId = "4"), FavoriteSongEntity(songId = "4"))

        try {
            dao.replaceAll(entities)
            throw AssertionError("expected the duplicate primary key to abort the insert")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // 失敗したinsertの前のdeleteAll()はロールバックされていなければならない。
        }

        assertEquals(listOf(SongId("1"), SongId("2"), SongId("3")), repository.findAll())
    }

    @Test
    fun toggleAddsWhenAbsentAndRemovesWhenPresent() = runTest {
        repository.toggle(SongId("1"))
        assertTrue(repository.exists(SongId("1")))

        repository.toggle(SongId("1"))
        assertFalse(repository.exists(SongId("1")))
    }

    /**
     * exists()とadd()を別々に呼ぶと間に中断点ができ、
     * 同時に2回toggleすると両方が「未登録」と判断して挿入に進む。
     * 主キーがあるので重複行にはならないがクラッシュする。
     * toggleはそれを防ぐためにある。
     */
    @Test
    fun concurrentTogglesNeverInsertTheSameSongTwice() = runBlocking {
        repeat(100) {
            db.favoriteSongDao().deleteAll()

            (1..2).map {
                async(Dispatchers.Default) { repository.toggle(SongId("1")) }
            }.awaitAll()

            assertTrue(db.favoriteSongDao().findAll().size <= 1)
        }
    }

    /**
     * 主キーにより重複行は作れない。
     */
    @Test
    fun theSameSongCannotBeStoredTwice() = runTest {
        val dao = db.favoriteSongDao()
        dao.insert(FavoriteSongEntity(songId = "1"))

        try {
            dao.insert(FavoriteSongEntity(songId = "1"))
            throw AssertionError("expected the primary key to reject the duplicate song")
        } catch (expected: android.database.sqlite.SQLiteConstraintException) {
            // 主キーの重複なので失敗するのが正しい。
        }

        assertEquals(listOf(SongId("1")), repository.findAll())
    }

    @Test
    fun deleteByIdsRemovesOnlyTheGivenIds() = runTest {
        repository.replaceAll(listOf(SongId("1"), SongId("2"), SongId("3")))

        repository.deleteByIds(listOf(SongId("1"), SongId("3")))

        assertEquals(listOf(SongId("2")), repository.findAll())
    }
}
