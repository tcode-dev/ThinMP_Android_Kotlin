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
 * 「最近追加された」アルバムは、アルバムの_idでは並べられない。
 * MediaStoreはアルバム名からidを計算するので、削除して追加し直したアルバムも以前と同じidになり、
 * idの大小は追加日時と関係がない。
 * アルバムのテーブルには日付の列もないので、曲の追加日時から順序を決める。
 *
 * 3つのアルバムを古い順に追加し、新しい順に返ることを確かめる(この順はidの順と一致しない)。
 *
 * 端末に音声ファイルが必要で、なければスキップする(tools/push-test-audio.sh)。
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
     * アルバムの新しさは、その中で最も新しい曲で決まり、曲がいくつあってもアルバムは1回だけ返る。
     * グループ化はSQLで行うので、MediaStoreがGROUP BYに従っていることの確認も兼ねる。
     * 従わなければ2曲あるアルバムが2回返る。
     */
    @Test
    fun anAlbumIsAsRecentAsItsNewestTrack() = runBlocking {
        insertTrack("a")
        insertTrack("b")
        insertTrack("a", suffix = "2")
        assumeTrue("MediaStore gave the three tracks the same date_added", addedDates().distinct().size == 3)

        val albums = MainService(context).getRecentlyAlbums().filter { it.name.startsWith(ALBUM_PREFIX) }

        assertEquals(listOf("${ALBUM_PREFIX}a", "${ALBUM_PREFIX}b"), albums.map { it.name })
    }

    /**
     * date_addedは秒単位なので、順序が付くように間隔を空けて挿入する。
     * sleepだけに頼らず、テスト側で値がすべて異なることを確かめる。
     */
    private fun insertTrack(album: String, suffix: String = "") {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$TITLE_PREFIX$album$suffix.mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Audio.Media.TITLE, "$TITLE_PREFIX$album$suffix")
            put(MediaStore.Audio.Media.ARTIST, "thinmp_test_recent_artist")
            put(MediaStore.Audio.Media.ALBUM, "$ALBUM_PREFIX$album")
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

    /** 前回の実行がtearDownの前に止まっていても影響しないよう、挿入の前にも呼ぶ。 */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }
}
