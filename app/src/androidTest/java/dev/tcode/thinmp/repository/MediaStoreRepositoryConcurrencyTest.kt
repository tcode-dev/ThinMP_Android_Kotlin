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

/** 競合が確実に起きる程度に多く、テストが遅くならない程度に少ない回数。 */
private const val ROUNDS = 20

/**
 * MediaStoreRepositoryはselection、引数、ソート順、カーソルをフィールドに持たない。
 * フィールドに持つと、1つのインスタンスを使う複数のコルーチンが互いのselectionを上書きしたり、
 * 読み込み中のカーソルを閉じたりする。
 *
 * どちらのテストも1つのインスタンスに3つのコルーチンから同時にクエリを投げる。
 * フィールドに持つ実装なら、他のコルーチンの行が返るか、閉じられたカーソルで例外になって失敗する。
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

    /** アルバムが別々の3曲。アルバムidで検索すると必ず1行だけ返る。 */
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

    /** 前回の実行がtearDownの前に止まっていても影響しないよう、挿入の前にも呼ぶ。 */
    private fun deleteTracks() {
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Audio.Media.TITLE} LIKE ?", arrayOf("$TITLE_PREFIX%")
        )
    }
}
