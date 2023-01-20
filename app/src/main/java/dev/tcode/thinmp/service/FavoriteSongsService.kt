package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.repository.realm.FavoriteSongRepository

class FavoriteSongsService(val context: Context) {
    fun findAll(): List<SongModel> {
        val favoriteSongRepository = FavoriteSongRepository()
        val songIds = favoriteSongRepository.findAll()
        val songRepository = SongRepository(context)

        return songRepository.findByIds(songIds)
    }
}
