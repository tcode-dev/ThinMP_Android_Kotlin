package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.repository.media.ArtistRepository
import dev.tcode.thinmp.repository.realm.FavoriteArtistRepository

class FavoriteArtistsService(val context: Context) {
    fun findAll(): List<ArtistModel> {
        val favoriteArtistRepository = FavoriteArtistRepository()
        val artistIds = favoriteArtistRepository.findAll()
        val artistRepository = ArtistRepository(context)

        return artistRepository.findByIds(artistIds)
    }
}