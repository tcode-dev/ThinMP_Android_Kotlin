package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.FavoriteArtistRepository

interface FavoriteArtistRegister {
    suspend fun existsFavoriteArtist(artistId: ArtistId): Boolean {
        val repository = FavoriteArtistRepository()

        return repository.exists(artistId)
    }

    suspend fun addFavoriteArtist(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.add(artistId)
    }

    suspend fun deleteFavoriteArtist(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.delete(artistId)
    }

    suspend fun replaceFavoriteArtists(artistIds: List<ArtistId>) {
        val repository = FavoriteArtistRepository()

        repository.replaceAll(artistIds)
    }
}
