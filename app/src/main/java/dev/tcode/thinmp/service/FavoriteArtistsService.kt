package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.repository.ArtistRepository
import dev.tcode.thinmp.repository.FavoriteArtistRepository

class FavoriteArtistsService(
    val context: Context,
    private val favoriteArtistRepository: FavoriteArtistRepository = FavoriteArtistRepository(),
    private val artistRepository: ArtistRepository = ArtistRepository(context)
) {
    suspend fun findAll(): List<ArtistModel> {
        val artistIds = favoriteArtistRepository.findAll()
        val artists = artistRepository.findByIds(artistIds)
        val found = artistIds.mapNotNull { id ->
            artists.find { it.artistId == id }
        }

        removeMissing(artistIds, artists)

        return found
    }

    /**
     * 再取得しない理由と、問い合わせが失敗したときに削除しない理由はFavoriteSongsService.removeMissing()を参照。
     */
    private suspend fun removeMissing(artistIds: List<ArtistId>, artists: List<ArtistModel>) {
        val deleteIds = artistIds.filter { id ->
            artists.none { it.artistId == id }
        }

        if (deleteIds.isEmpty()) return

        favoriteArtistRepository.deleteByIds(deleteIds)
    }
}