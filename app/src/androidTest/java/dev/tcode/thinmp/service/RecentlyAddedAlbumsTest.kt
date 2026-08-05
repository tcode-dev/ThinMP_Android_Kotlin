package dev.tcode.thinmp.service

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TITLE_PREFIX = "thinmp_test_recent_"
private const val ALBUM_PREFIX = "thinmp_test_recent_album_"

/**
 * "Recently added" used to sort the albums collection by its own _id, taking a larger id to mean a
 * newer album. MediaStore computes that id from the album's name: delete an album and add it back
 * and it comes back with the id it had before, so the order had nothing to do with when anything
 * was added. The albums collection has no date column of its own, so the order has to come from
 * the tracks.
 *
 * The three albums below are added oldest first and expected back newest first, which is the order
 * their ids happen not to be in.
 *
 * Needs at least one audio file on the device; skipped otherwise (tools/push-test-audio.sh).
 */
@RunWith(AndroidJUnit4::class)
class RecentlyAddedAlbumsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        deleteTracks()
    }

    @After
    fun tearDown() {
        deleteTracks()
    }

    @Test
    fun recentlyAddedAlbumsComeBackNewestFirst() = runBlocking {
        insertTrack("a")
        insertTrack("b")
        insertTrack("c")
        assumeTrue("MediaStore gave the three tracks the same date_added", addedDates().distinct().size == 3)

        val albums = MainService(context).getRecentlyAlbums().filter { it.name.startsWith(ALBUM_PREFIX) }

        assertEquals(listOf("${ALBUM_PREFIX}c", "${ALBUM_PREFIX}b", "${ALBUM_PREFIX}a"), albums.map { it.name })
    }

    /**
     * date_added counts in seconds, so the tracks have to be spaced out to be ordered at all. The
     * test asserts they came out distinct rather than trusting the sleep.
     */
    private fun insertTrack(suffix: String) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$TITLE_PREFIX$suffix.mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.TITLE, "$TITLE_PREFIX$suffix")
            put(MediaStore.Audio.Media.ARTIST, "thinmp_test_recent_artist")
            put(MediaStore.Audio.Media.ALBUM, "$ALBUM_PREFIX$suffix")
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
        }

        context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        Thread.sleep(1_100)
    }

    private fun addedDates(): List<Long> {
        val dates = mutableListOf<Long>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.DATE_ADDED),
            "${MediaStore.Audio.Media.TITLE} LIKE ?",
            arrayOf("$TITLE_PREFIX%"),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                dates.add(cursor.getLong(0))
            }
        }

        return dates
    }

    /** Also runs before the inserts: a run that died before its tearDown must not skew the next. */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }
}
