package dev.tcode.thinmp.repository

import dev.tcode.thinmp.application.MainApplication
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.room.FavoriteArtistEntity
import dev.tcode.thinmp.repository.dao.FavoriteArtistDao

class FavoriteArtistRepository(
    private val dao: FavoriteArtistDao = AppDatabase.getDatabase(MainApplication.appContext).favoriteArtistDao()
) {
    suspend fun exists(artistId: ArtistId): Boolean {
        return dao.exists(artistId.id)
    }

    suspend fun findAll(): List<ArtistId> {
        return dao.findAll().map { ArtistId(it.artistId) }
    }

    suspend fun add(_artistId: ArtistId) {
        dao.insert(FavoriteArtistEntity(artistId = _artistId.id))
    }

    suspend fun replaceAll(artistIds: List<ArtistId>) {
        dao.replaceAll(artistIds.map { FavoriteArtistEntity(artistId = it.id) })
    }

    suspend fun delete(artistId: ArtistId) {
        dao.deleteByArtistId(artistId.id)
    }

    suspend fun deleteByIds(artistIds: List<ArtistId>) {
        dao.deleteByArtistIds(artistIds.map { it.id })
    }
}
