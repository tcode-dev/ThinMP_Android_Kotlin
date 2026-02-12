package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.room.FavoriteArtistRepository

interface FavoriteArtistRegister {
    fun exists(artistId: ArtistId): Boolean {
        val repository = FavoriteArtistRepository()

        return repository.exists(artistId)
    }

    fun add(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.add(artistId)
    }

    fun delete(artistId: ArtistId) {
        val repository = FavoriteArtistRepository()

        repository.delete(artistId)
    }

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("updateArtists")
    fun update(artistIds: List<ArtistId>) {
        val repository = FavoriteArtistRepository()

        repository.update(artistIds)
    }
}