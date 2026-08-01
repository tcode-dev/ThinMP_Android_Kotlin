package dev.tcode.thinmp.repository

import dev.tcode.thinmp.application.MainApplication
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.FavoriteSongEntity
import dev.tcode.thinmp.repository.dao.FavoriteSongDao

class FavoriteSongRepository(
    private val dao: FavoriteSongDao = AppDatabase.getDatabase(MainApplication.appContext).favoriteSongDao()
) {
    suspend fun exists(songId: SongId): Boolean {
        return dao.exists(songId.id)
    }

    suspend fun findAll(): List<SongId> {
        return dao.findAll().map { SongId(it.songId) }
    }

    suspend fun add(_songId: SongId) {
        dao.insert(FavoriteSongEntity(songId = _songId.id))
    }

    suspend fun replaceAll(songIds: List<SongId>) {
        dao.replaceAll(songIds.map { FavoriteSongEntity(songId = it.id) })
    }

    suspend fun delete(songId: SongId) {
        dao.deleteBySongId(songId.id)
    }

    suspend fun deleteByIds(songIds: List<SongId>) {
        dao.deleteBySongIds(songIds.map { it.id })
    }
}
