package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.FavoriteArtistRepository

interface FavoriteArtistRegister {
    suspend fun existsFavoriteArtist(artistId: ArtistId): Boolean {
        val repository = FavoriteArtistRepository()

        return repository.exists(artistId)
    }

    /**
     * The only way to flip one artist's favourite state. Deliberately not exposed as separate add
     * and delete: reading existsFavoriteArtist() and then writing leaves a suspension point
     * between the two, so a double tap inserts the artist twice.
     */
    suspend fun toggleFavoriteArtist(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.toggle(artistId)
    }

    suspend fun replaceFavoriteArtists(artistIds: List<ArtistId>) {
        val repository = FavoriteArtistRepository()

        repository.replaceAll(artistIds)
    }
}
