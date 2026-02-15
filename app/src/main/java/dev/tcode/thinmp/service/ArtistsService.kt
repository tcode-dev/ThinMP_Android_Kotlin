package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.repository.ArtistRepository

class ArtistsService(val context: Context) {
    fun findAll(): List<ArtistModel> {
        val repository = ArtistRepository(context)
        val artists = repository.findAll()

        return artists.sortedBy { it.name }
    }
}