package dev.tcode.thinmp.viewModel

import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 表示したときの位置と、タップしたときにview modelが持つ一覧は、その間に読み込み直しが入るとずれる。
 * 位置で再生すると、範囲外ならサービスでIllegalSeekPositionException、範囲内なら違う曲の再生になる。
 */
class IndexOfSongTest {
    private val a = song("1")
    private val b = song("2")
    private val c = song("3")
    private val x = song("9")

    @Test
    fun isThePositionOfTheSongInTheList() {
        assertEquals(0, indexOfSong(listOf(a, b, c), a.songId))
        assertEquals(1, indexOfSong(listOf(a, b, c), b.songId))
        assertEquals(2, indexOfSong(listOf(a, b, c), c.songId))
    }

    /** [a, b, c]の1行目のbをタップしたが、一覧は[x, a, b, c]に変わっていた。1行目はもうaを指す。 */
    @Test
    fun followsTheSongWhenAReloadMovesIt() {
        assertEquals(2, indexOfSong(listOf(x, a, b, c), b.songId))
    }

    /** [a, b, c]の2行目のcをタップしたが、一覧は[a]に変わっていた。2行目はもうない。 */
    @Test
    fun isNullWhenAReloadDroppedTheSong() {
        assertNull(indexOfSong(listOf(a), c.songId))
    }

    @Test
    fun isNullForAnEmptyList() {
        assertNull(indexOfSong(emptyList(), a.songId))
    }

    /** MediaStoreはクエリのたびに新しいオブジェクトを返すので、読み込み直しても同じなのはidだけ。 */
    @Test
    fun matchesByIdAndNotByInstance() {
        assertEquals(1, indexOfSong(listOf(a, song("2"), c), b.songId))
    }

    private fun song(id: String): SongModel {
        return SongModel(SongId(id), "song $id", ArtistId("a"), "artist", AlbumId("b"), "album", 0, "")
    }
}
