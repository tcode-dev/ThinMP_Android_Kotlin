package dev.tcode.thinmp.service

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.FavoriteSongEntity
import dev.tcode.thinmp.repository.AppDatabase
import dev.tcode.thinmp.repository.FavoriteSongRepository
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.createTestDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * These services used to compare an id count against a MediaStore row count and re-enter
 * themselves whenever the two disagreed. A duplicated id makes them disagree permanently - SQL IN
 * collapses the duplicate - so the cleanup deleted nothing and the call never converged. It did
 * not crash, it span, which is why the screen simply never finished loading.
 *
 * Needs at least one audio file on the device; skipped otherwise.
 */
@RunWith(AndroidJUnit4::class)
class DuplicateEntryServiceTest {
    private val timeoutMs = 10_000L

    private lateinit var db: AppDatabase
    private lateinit var context: Context
    private var realSongIdValue: String = ""
    private val realSongId: SongId get() = SongId(realSongIdValue)

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        db = createTestDatabase()

        val songs = SongRepository(context).findAll()
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        realSongIdValue = songs.first().songId.id
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** Adding the same song to a playlist twice is an ordinary thing to do. */
    @Test
    fun playlistHoldingTheSameSongTwiceStillLoads() = runBlocking {
        val repository = PlaylistRepository(db)
        repository.create(realSongId, "duplicate")
        val playlistId = PlaylistId(repository.findAll().first().id)
        repository.add(playlistId, realSongId)

        val playlist = withTimeout(timeoutMs) { PlaylistDetailService(context, repository).findById(playlistId) }

        // The playlist says the song is in it twice, so the detail screen shows it twice.
        assertEquals(2, playlist?.songs?.size)
    }

    /**
     * Unlike a playlist, a song is either a favourite or it is not, so the songId is the primary
     * key and the duplicate this test used to construct is no longer a state the schema allows.
     */
    @Test
    fun duplicateFavouriteRowCannotBeCreated() = runBlocking {
        db.favoriteSongDao().insert(FavoriteSongEntity(songId = realSongIdValue))

        try {
            db.favoriteSongDao().insert(FavoriteSongEntity(songId = realSongIdValue))
            throw AssertionError("expected the primary key to reject the duplicate favourite")
        } catch (expected: SQLiteConstraintException) {
            // Nothing to clean up: the second row was never written.
        }

        val songs = withTimeout(timeoutMs) {
            FavoriteSongsService(context, FavoriteSongRepository(db.favoriteSongDao())).findAll()
        }

        assertEquals(1, songs.size)
    }

    /** The cleanup the recursion existed to re-read after still has to happen. */
    @Test
    fun favouriteWhoseFileIsGoneIsDropped() = runBlocking {
        val repository = FavoriteSongRepository(db.favoriteSongDao())
        repository.add(realSongId)
        repository.add(SongId("-1"))

        val songs = withTimeout(timeoutMs) { FavoriteSongsService(context, repository).findAll() }

        assertEquals(1, songs.size)
        assertTrue(repository.findAll().none { it.id == "-1" })
    }
}
