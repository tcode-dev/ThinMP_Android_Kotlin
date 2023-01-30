package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.realm.FavoriteSongRepository

interface FavoriteSongRegister {
    fun existsFavorite(songId: SongId): Boolean {
        val repository = FavoriteSongRepository()

        return repository.exists(songId)
    }

    fun addFavorite(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.add(songId)
    }

    fun deleteFavorite(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.delete(songId)
    }
}