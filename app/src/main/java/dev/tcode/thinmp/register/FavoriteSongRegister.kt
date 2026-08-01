package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.FavoriteSongRepository

interface FavoriteSongRegister {
    suspend fun existsFavoriteSong(songId: SongId): Boolean {
        val repository = FavoriteSongRepository()

        return repository.exists(songId)
    }

    suspend fun addFavoriteSong(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.add(songId)
    }

    suspend fun deleteFavoriteSong(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.delete(songId)
    }

    suspend fun replaceFavoriteSongs(songIds: List<SongId>) {
        val repository = FavoriteSongRepository()

        repository.replaceAll(songIds)
    }
}
