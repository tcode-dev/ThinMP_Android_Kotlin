package dev.tcode.thinmp.repository

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TITLE_PREFIX = "thinmp_test_concurrent_"

/** Enough rounds that an interleaving has to happen, short enough to stay a fast test. */
private const val ROUNDS = 20

/**
 * MediaStoreRepository used to keep the selection, its arguments, the sort order and the open
 * cursor in fields, so two coroutines sharing one instance shared those too: each query overwrote
 * the other's selection before the cursor was created, and closed the other's cursor while it was
 * still being read. The repository is created per call today, which is why nothing on screen showed
 * it - the fields still made a single instance unusable from two places at once.
 *
 * Both tests below query one instance from three coroutines at a time. Without the fix they fail
 * either on the assertion - a coroutine handed another's rows - or on the cursor the other coroutine
 * closed underneath it.
 */
@RunWith(AndroidJUnit4::class)
class MediaStoreRepositoryConcurrencyTest {
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
    fun concurrentListQueriesOnOneInstanceEachSeeTheirOwnRows() = runBlocking {
        val songs = insertTracks()
        val repository = SongRepository(context)

        repeat(ROUNDS) {
            val found = coroutineScope {
                songs.map { song ->
                    async(Dispatchers.Default) { song.name to repository.findByAlbumId(song.albumId.id).map { it.name } }
                }.awaitAll()
            }

            found.forEach { (name, names) -> assertEquals(listOf(name), names) }
        }
    }

    @Test
    fun concurrentSingleRowQueriesOnOneInstanceEachSeeTheirOwnRow() = runBlocking {
        val songs = insertTracks()
        val repository = SongRepository(context)

        repeat(ROUNDS) {
            val found = coroutineScope {
                songs.map { song ->
                    async(Dispatchers.Default) { song.name to repository.findById(song.songId.id)?.name }
                }.awaitAll()
            }

            found.forEach { (name, foundName) -> assertEquals(name, foundName) }
        }
    }

    /** Three tracks, each its own album, so a query by album id has exactly one row to come back with. */
    private suspend fun insertTracks(): List<SongModel> {
        listOf("a", "b", "c").forEach { name ->
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "$TITLE_PREFIX$name.mp3")
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.TITLE, "$TITLE_PREFIX$name")
                put(MediaStore.Audio.Media.ARTIST, "${TITLE_PREFIX}artist_$name")
                put(MediaStore.Audio.Media.ALBUM, "${TITLE_PREFIX}album_$name")
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
            }

            context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        }

        return SongRepository(context).findAll().filter { it.name.startsWith(TITLE_PREFIX) }
    }

    /** Also runs before the inserts: a run that died before its tearDown must not skew the next. */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }
}
