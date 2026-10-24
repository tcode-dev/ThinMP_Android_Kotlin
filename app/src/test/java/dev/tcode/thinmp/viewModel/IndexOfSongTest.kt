package dev.tcode.thinmp.viewModel

import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The reloaded-list cases are the ones that matter. A position taken when the row was composed and
 * the list the view model holds when the tap arrives agree until a reload lands in between: a
 * position past the new end would be an IllegalSeekPositionException inside the service, and one
 * within it the wrong song.
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

    /** Tapped b on row 1 of [a, b, c]; the list came back as [x, a, b, c]. Row 1 is now a. */
    @Test
    fun followsTheSongWhenAReloadMovesIt() {
        assertEquals(2, indexOfSong(listOf(x, a, b, c), b.songId))
    }

    /** Tapped c on row 2 of [a, b, c]; the list came back as [a]. Row 2 no longer exists. */
    @Test
    fun isNullWhenAReloadDroppedTheSong() {
        assertNull(indexOfSong(listOf(a), c.songId))
    }

    @Test
    fun isNullForAnEmptyList() {
        assertNull(indexOfSong(emptyList(), a.songId))
    }

    /** MediaStore hands out a fresh object per query, so only the id can survive a reload. */
    @Test
    fun matchesByIdAndNotByInstance() {
        assertEquals(1, indexOfSong(listOf(a, song("2"), c), b.songId))
    }

    private fun song(id: String): SongModel {
        return SongModel(SongId(id), "song $id", ArtistId("a"), "artist", AlbumId("b"), "album", 0, "")
    }
}
