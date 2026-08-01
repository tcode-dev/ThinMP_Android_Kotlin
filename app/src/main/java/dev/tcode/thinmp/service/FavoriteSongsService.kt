package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.FavoriteSongRepository

class FavoriteSongsService(val context: Context, private val favoriteSongRepository: FavoriteSongRepository = FavoriteSongRepository()) {
    suspend fun findAll(): List<SongModel> {
        val songIds = favoriteSongRepository.findAll()
        val songRepository = SongRepository(context)
        val songs = songRepository.findByIds(songIds)
        val found = songIds.mapNotNull { id ->
            songs.find { it.songId == id }
        }

        removeMissing(songIds, songs)

        return found
    }

    /**
     * Drops favourites whose file has left MediaStore. This used to compare songIds.count() with
     * songs.count() and re-enter findAll() on a mismatch, which never terminated when the counts
     * differed because of a duplicate row rather than a missing file: the ids were all still
     * present, so nothing was deleted and the next call saw the same state.
     *
     * The re-read was unnecessary anyway. Deleting rows that produced no SongModel cannot change
     * `found`, so the caller already has the right answer before this runs.
     */
    private suspend fun removeMissing(songIds: List<SongId>, songs: List<SongModel>) {
        val deleteIds = songIds.filter { id ->
            songs.none { it.songId == id }
        }

        if (deleteIds.isEmpty()) return

        favoriteSongRepository.deleteByIds(deleteIds)
    }
}