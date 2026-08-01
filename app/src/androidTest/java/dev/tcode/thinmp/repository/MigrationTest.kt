package dev.tcode.thinmp.repository

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration_test"

/**
 * `runMigrationsAndValidate` compares the migrated database against the exported schema, so it is
 * what catches a migration whose index name or column list has drifted from the entity annotation.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2AddsIndexesAndKeepsRows() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO favorite_songs (id, songId) VALUES ('a', '1')")
            execSQL("INSERT INTO favorite_artists (id, artistId) VALUES ('b', '2')")
            execSQL("INSERT INTO playlists (id, name, `order`) VALUES ('c', 'playlist', 1)")
            execSQL("INSERT INTO playlist_songs (id, playlistId, songId) VALUES ('d', 'c', '1')")
            execSQL("INSERT INTO shortcuts (id, itemId, type, `order`) VALUES ('e', '3', 0, 1)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2)

        db.query("SELECT songId FROM favorite_songs").use {
            assertTrue(it.moveToFirst())
            assertEquals("1", it.getString(0))
        }

        // A duplicate row is still allowed: the indexes are deliberately not UNIQUE, because
        // databases written before FavoriteSongDao.toggle existed may already hold one.
        db.execSQL("INSERT INTO favorite_songs (id, songId) VALUES ('f', '1')")

        db.close()
    }
}
