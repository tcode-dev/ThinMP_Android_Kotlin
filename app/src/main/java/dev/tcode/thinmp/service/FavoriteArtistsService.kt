package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.ArtistRepository
import dev.tcode.thinmp.repository.FavoriteArtistRepository

class FavoriteArtistsService(val context: Context, private val favoriteArtistRepository: FavoriteArtistRepository = FavoriteArtistRepository()) {
    suspend fun findAll(): List<ArtistModel> {
        val artistIds = favoriteArtistRepository.findAll()
        val artistRepository = ArtistRepository(context)
        val artists = artistRepository.findByIds(artistIds)
        val found = artistIds.mapNotNull { id ->
            artists.find { it.artistId == id }
        }

        removeMissing(artistIds, artists)

        return found
    }

    /** See FavoriteSongsService.removeMissing() for why this no longer re-reads. */
    private suspend fun removeMissing(artistIds: List<ArtistId>, artists: List<ArtistModel>) {
        val deleteIds = artistIds.filter { id ->
            artists.none { it.artistId == id }
        }

        if (deleteIds.isEmpty()) return

        favoriteArtistRepository.deleteByIds(deleteIds)
    }
}