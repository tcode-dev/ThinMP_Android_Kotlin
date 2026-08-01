package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.FavoriteArtistRepository

interface FavoriteArtistRegister {
    fun existsFavoriteArtist(artistId: ArtistId): Boolean {
        val repository = FavoriteArtistRepository()

        return repository.exists(artistId)
    }

    fun addFavoriteArtist(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.add(artistId)
    }

    fun deleteFavoriteArtist(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.delete(artistId)
    }

    fun replaceFavoriteArtists(artistIds: List<ArtistId>) {
        val repository = FavoriteArtistRepository()

        repository.replaceAll(artistIds)
    }
}
