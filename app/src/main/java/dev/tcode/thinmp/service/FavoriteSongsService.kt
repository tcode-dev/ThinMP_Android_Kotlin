package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.repository.realm.FavoriteSongRepository

class FavoriteSongsService(val context: Context, private val favoriteSongRepository: FavoriteSongRepository = FavoriteSongRepository()) {
    fun findAll(): List<SongModel> {
        val songIds = favoriteSongRepository.findAll()
        val songRepository = SongRepository(context)
        val songs = songRepository.findByIds(songIds)

        if (!validation(songIds, songs)) {
            fix(songIds, songs)

            return findAll()
        }

        return songs
    }

    private fun validation(songIds: List<SongId>, songs: List<SongModel>): Boolean {
        return songIds.count() == songs.count()
    }

    private fun fix(songIds: List<SongId>, songs: List<SongModel>) {
        val deleteIds = songIds.filter { id ->
            songs.none { it.songId == id }
        }

        favoriteSongRepository.update(deleteIds)
    }
}