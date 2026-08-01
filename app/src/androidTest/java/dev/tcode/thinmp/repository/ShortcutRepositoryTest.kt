package dev.tcode.thinmp.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.constant.ItemType
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.ShortcutId
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
        // Same raw id, different type: must not collide.
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
        // findAll is ordered by `order` descending, so this is 3, 2, 1.
        val ids = repository.findAll().map { ShortcutId(it.id) }

        repository.reorder(listOf(ids[0], ids[2]))

        val shortcuts = repository.findAll()

        assertEquals(2, shortcuts.size)
        assertEquals(listOf(ids[2].id, ids[0].id), shortcuts.map { it.id })
        assertEquals(listOf(2, 1), shortcuts.map { it.order })
    }
}
