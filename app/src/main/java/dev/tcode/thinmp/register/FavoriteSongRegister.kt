package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.FavoriteSongRepository

interface FavoriteSongRegister {
    suspend fun existsFavoriteSong(songId: SongId): Boolean {
        val repository = FavoriteSongRepository()

        return repository.exists(songId)
    }

    /**
     * The only way to flip one song's favourite state. Deliberately not exposed as separate add
     * and delete: reading existsFavoriteSong() and then writing leaves a suspension point between
     * the two, so a double tap inserts the song twice.
     */
    suspend fun toggleFavoriteSong(songId: SongId) {
        val repository = FavoriteSongRepository()

        repository.toggle(songId)
    }

    suspend fun replaceFavoriteSongs(songIds: List<SongId>) {
        val repository = FavoriteSongRepository()

        repository.replaceAll(songIds)
    }
}
