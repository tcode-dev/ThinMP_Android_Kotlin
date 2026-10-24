package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.constant.ItemType
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.ShortcutId
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
class ShortcutRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: ShortcutRepository

    @Before
    fun setUp() {
        db = createTestDatabase()
        repository = ShortcutRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addAndDeleteAreScopedToTheItemType() = runTest {
        repository.add(ArtistId("1"))

        assertTrue(repository.exists(ArtistId("1")))
        // idが同じでもtypeが違えば別のショートカット。
        assertFalse(repository.exists(AlbumId("1")))

        repository.delete(ArtistId("1"))
        assertFalse(repository.exists(ArtistId("1")))
    }

    @Test
    fun addNumbersShortcutsFromOne() = runTest {
        repository.add(ArtistId("1"))
        repository.add(AlbumId("2"))
        repository.add(ArtistId("3"))

        assertEquals(listOf(3, 2, 1), repository.findAll().map { it.order })
    }

    @Test
    fun addRecordsTheItemType() = runTest {
        repository.add(AlbumId("1"))

        assertEquals(ItemType.ALBUM.ordinal, repository.findAll().first().type)
    }

    @Test
    fun reorderRenumbersSurvivorsAndDropsTheRest() = runTest {
        repository.add(ArtistId("1"))
        repository.add(ArtistId("2"))
        repository.add(ArtistId("3"))
        // findAllは`order`の降順なので3、2、1の順。
        val ids = repository.findAll().map { ShortcutId(it.id) }

        repository.reorder(listOf(ids[0], ids[2]))

        val shortcuts = repository.findAll()

        assertEquals(2, shortcuts.size)
        assertEquals(listOf(ids[2].id, ids[0].id), shortcuts.map { it.id })
        assertEquals(listOf(2, 1), shortcuts.map { it.order })
    }

    /** PlaylistRepositoryTest.reorderIgnoresAnIdThatIsNoLongerThereを参照。 */
    @Test
    fun reorderIgnoresAnIdThatIsNoLongerThere() = runTest {
        repository.add(ArtistId("1"))
        repository.add(ArtistId("2"))
        val ids = repository.findAll().map { ShortcutId(it.id) }

        repository.reorder(listOf(ids[0], ShortcutId("gone"), ids[1]))

        val shortcuts = repository.findAll()

        assertEquals(2, shortcuts.size)
        assertEquals(listOf(ids[1].id, ids[0].id), shortcuts.map { it.id })
        assertEquals(listOf(2, 1), shortcuts.map { it.order })
    }

    @Test
    fun toggleAddsWhenAbsentAndRemovesWhenPresent() = runTest {
        repository.toggle(ArtistId("1"))
        assertTrue(repository.exists(ArtistId("1")))
        assertFalse(repository.exists(AlbumId("1")))

        repository.toggle(ArtistId("1"))
        assertFalse(repository.exists(ArtistId("1")))
    }

    @Test
    fun toggleAppendsAtTheEnd() = runTest {
        repository.add(ArtistId("1"))

        repository.toggle(AlbumId("2"))

        assertEquals(listOf(2, 1), repository.findAll().map { it.order })
    }

    /**
     * exists()とadd()を別々に呼ぶと間に中断点ができ、
     * 同時に2回toggleすると両方が「未登録」と判断して2行目を挿入してしまう。
     * shortcutsのスキーマには重複を防ぐ制約がない。
     * toggleはそれを防ぐためにある。
     */
    @Test
    fun concurrentTogglesNeverInsertTheSameItemTwice() = runBlocking {
        repeat(100) {
            db.shortcutDao().deleteByIds(repository.findAll().map { it.id })

            (1..2).map {
                async(Dispatchers.Default) { repository.toggle(ArtistId("1")) }
            }.awaitAll()

            assertTrue(repository.findAll().size <= 1)
        }
    }
}
