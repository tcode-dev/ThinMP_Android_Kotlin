package dev.tcode.thinmp.repository

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.SongModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TITLE_PREFIX = "thinmp_test_order_"
private const val ALBUM = "thinmp_test_order_album"

/**
 * アルバムの曲は、ディスク番号、トラック番号の順に並ぶ。
 * トラック番号だけで並べると、複数ディスクのアルバムでディスクをまたいで混ざる。
 *
 * タグの値が列に入るので、どちらの番号も"1/2"のように全体の数を含むことがあり、数値として比べなければならない。
 * 挿入の順に並んで通ることがないよう、正しい順とは違う順で挿入する。
 *
 * 行はテストが挿入するので、端末に音声ファイルは要らない。
 */
@RunWith(AndroidJUnit4::class)
class SongRepositorySortOrderTest {
    private lateinit var context: Context
    private lateinit var repository: SongRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, "android.permission.READ_MEDIA_AUDIO")
        repository = SongRepository(context)
        deleteTracks()
    }

    @After
    fun tearDown() {
        deleteTracks()
    }

    @Test
    fun anAlbumIsOrderedByDiscThenTrack() = runBlocking {
        insertTrack("d2t1", "2", "1")
        insertTrack("d1t2", "1", "2")
        insertTrack("d2t2", "2", "2")
        insertTrack("d1t1", "1", "1")

        assertEquals(listOf("d1t1", "d1t2", "d2t1", "d2t2"), names(repository.findByAlbumId(insertedSong().albumId.id)))
    }

    /**
     * 文字列として比べると、ディスク10がディスク2より、トラック10がトラック2より前になる。
     */
    @Test
    fun numbersAreComparedAsNumbersEvenWithATotal() = runBlocking {
        insertTrack("d10t1", "10/10", "1/12")
        insertTrack("d2t10", "2/10", "10/12")
        insertTrack("d2t2", "2/10", "2/12")

        assertEquals(listOf("d2t2", "d2t10", "d10t1"), names(repository.findByAlbumId(insertedSong().albumId.id)))
    }

    /**
     * ディスク番号のタグがないアルバムは、トラック番号の順に並ぶ。
     */
    @Test
    fun anAlbumWithoutDiscNumbersIsOrderedByTrack() = runBlocking {
        insertTrack("t2", null, "2")
        insertTrack("t1", null, "1")

        assertEquals(listOf("t1", "t2"), names(repository.findByAlbumId(insertedSong().albumId.id)))
    }

    /**
     * アーティストの曲も、アルバムの中は同じ順に並ぶ。
     */
    @Test
    fun anArtistsSongsAreOrderedByDiscThenTrackWithinAnAlbum() = runBlocking {
        insertTrack("d2t1", "2", "1")
        insertTrack("d1t2", "1", "2")

        assertEquals(listOf("d1t2", "d2t1"), names(repository.findByArtistId(insertedSong().artistId.id)))
    }

    private suspend fun insertedSong(): SongModel {
        return repository.findAll().first { it.name.startsWith(TITLE_PREFIX) }
    }

    private fun names(songs: List<SongModel>): List<String> {
        return songs.map { it.name.removePrefix(TITLE_PREFIX) }
    }

    private fun insertTrack(name: String, disc: String?, track: String) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$TITLE_PREFIX$name.mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.TITLE, "$TITLE_PREFIX$name")
            put(MediaStore.Audio.Media.ARTIST, "thinmp_test_order_artist")
            put(MediaStore.Audio.Media.ALBUM, ALBUM)
            put(MediaStore.Audio.Media.DISC_NUMBER, disc)
            put(MediaStore.Audio.Media.CD_TRACK_NUMBER, track)
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
        }

        context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
    }

    /**
     * 前回の実行がtearDownの前に止まっていても影響しないよう、挿入の前にも呼ぶ。
     */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }
}
