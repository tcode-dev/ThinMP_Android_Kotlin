package dev.tcode.thinmp.service

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.constant.ItemType
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.AlbumRepository
import dev.tcode.thinmp.repository.AppDatabase
import dev.tcode.thinmp.repository.ArtistRepository
import dev.tcode.thinmp.repository.PlaylistRepository
import dev.tcode.thinmp.repository.ShortcutRepository
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.createTestDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TITLE_PREFIX = "thinmp_test_compilation_"
private const val ALBUM = "thinmp_test_compilation"

/**
 * A shortcut to an artist takes its image from that artist's first album, and the code assumed
 * there always is one. The albums collection holds one row per album carrying one artist_id, so
 * every artist on a compilation except the one that row happens to name is an artist with no
 * album of their own. They are still listed under Artists, so they can be shortcut like any
 * other. first() threw NoSuchElementException there, and findAll() runs while the main screen
 * loads, so the crash took the whole screen.
 *
 * The two tracks below build exactly that state in MediaStore rather than standing in for the
 * repository, so the album lookup that comes back empty is the real one. Which of the two artists
 * ends up named by the album row is MediaStore's choice, so the test asks rather than assumes.
 */
@RunWith(AndroidJUnit4::class)
class ShortcutServiceTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var shortcuts: ShortcutRepository
    private var withoutAlbum: String = ""
    private var withAlbum: String = ""

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        db = createTestDatabase()
        shortcuts = ShortcutRepository(db)

        deleteTracks()
        insertTrack("${TITLE_PREFIX}a", "thinmp_test_artist_a")
        insertTrack("${TITLE_PREFIX}b", "thinmp_test_artist_b")

        val artistIds = SongRepository(context).findAll().filter { it.name.startsWith(TITLE_PREFIX) }.map { it.artistId.id }.distinct()
        assumeTrue("MediaStore did not keep the two tracks apart", artistIds.size == 2)

        val albums = AlbumRepository(context)
        withoutAlbum = artistIds.firstOrNull { albums.findByArtistId(it).isEmpty() } ?: ""
        withAlbum = artistIds.firstOrNull { albums.findByArtistId(it).isNotEmpty() } ?: ""
        assumeTrue("MediaStore gave both artists an album of their own", withoutAlbum.isNotEmpty() && withAlbum.isNotEmpty())
    }

    @After
    fun tearDown() {
        deleteTracks()
        db.close()
    }

    @Test
    fun anArtistWithNoAlbumFallsBackToAnEmptyImage() = runBlocking {
        shortcuts.add(ArtistId(withoutAlbum))

        val models = ShortcutService(context, context.resources, shortcuts, ArtistRepository(context), AlbumRepository(context), SongRepository(context), PlaylistRepository(db)).findAll()

        assertEquals(1, models.size)
        assertEquals(ItemType.ARTIST, models.first().type)
        assertEquals(Uri.EMPTY, models.first().imageUri)
    }

    /** The shortcut still resolves, so the cleanup must not take it for a stale row. */
    @Test
    fun anArtistWithNoAlbumIsKept() = runBlocking {
        shortcuts.add(ArtistId(withoutAlbum))

        ShortcutService(context, context.resources, shortcuts, ArtistRepository(context), AlbumRepository(context), SongRepository(context), PlaylistRepository(db)).findAll()

        assertEquals(1, shortcuts.findAll().size)
    }

    /** The image still comes from the artist's album when there is one. */
    @Test
    fun anArtistWithAnAlbumKeepsItsImage() = runBlocking {
        shortcuts.add(ArtistId(withAlbum))

        val models = ShortcutService(context, context.resources, shortcuts, ArtistRepository(context), AlbumRepository(context), SongRepository(context), PlaylistRepository(db)).findAll()

        assertEquals(1, models.size)
        assertNotEquals(Uri.EMPTY, models.first().imageUri)
    }

    /**
     * Both tracks land in the same album, so MediaStore has to pick one of the two artists for
     * that album's row and the other one is left without an album. The files stay empty: nothing
     * plays them, and a WAV carries no tag block to put an artist in anyway.
     */
    private fun insertTrack(title: String, artist: String) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$title.mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.TITLE, title)
            put(MediaStore.Audio.Media.ARTIST, artist)
            put(MediaStore.Audio.Media.ALBUM, ALBUM)
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
        }

        context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
    }

    /** Also runs before the inserts: a run that died before its tearDown must not skip the next. */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }
}
