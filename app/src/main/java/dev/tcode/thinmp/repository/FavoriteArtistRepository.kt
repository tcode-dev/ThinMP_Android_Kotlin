package dev.tcode.thinmp.repository

import dev.tcode.thinmp.application.MainApplication
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.room.FavoriteArtistEntity

class FavoriteArtistRepository {
    private val dao = AppDatabase.getDatabase(MainApplication.appContext).favoriteArtistDao()

    fun exists(artistId: ArtistId): Boolean {
        return dao.exists(artistId.id)
    }

    fun findAll(): List<ArtistId> {
        return dao.findAll().map { ArtistId(it.artistId) }
    }

    fun add(_artistId: ArtistId) {
        dao.insert(FavoriteArtistEntity(artistId = _artistId.id))
    }

    fun update(artistIds: List<ArtistId>) {
        dao.deleteAll()
        artistIds.forEach {
            dao.insert(FavoriteArtistEntity(artistId = it.id))
        }
    }

    fun delete(artistId: ArtistId) {
        dao.deleteByArtistId(artistId.id)
    }

    fun deleteByIds(artistIds: List<ArtistId>) {
        dao.deleteByArtistIds(artistIds.map { it.id })
    }
}
