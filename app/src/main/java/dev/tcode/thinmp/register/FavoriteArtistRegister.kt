package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.realm.FavoriteArtistRepository

interface FavoriteArtistRegister {
    fun existsFavorite(artistId: ArtistId): Boolean {
        val repository = FavoriteArtistRepository()

        return repository.exists(artistId)
    }

    fun addFavorite(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.add(artistId)
    }

    fun deleteFavorite(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.delete(artistId)
    }
}