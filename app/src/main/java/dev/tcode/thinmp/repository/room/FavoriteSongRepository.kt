package dev.tcode.thinmp.repository.room

import dev.tcode.thinmp.application.MainApplication
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.FavoriteSongEntity

class FavoriteSongRepository {
    private val dao = AppDatabase.getDatabase(MainApplication.appContext).favoriteSongDao()

    fun exists(songId: SongId): Boolean {
        return dao.exists(songId.id)
    }

    fun findAll(): List<SongId> {
        return dao.findAll().map { SongId(it.songId) }
    }

    fun add(_songId: SongId) {
        dao.insert(FavoriteSongEntity(songId = _songId.id))
    }

    fun update(songIds: List<SongId>) {
        dao.deleteAll()
        songIds.forEach {
            dao.insert(FavoriteSongEntity(songId = it.id))
        }
    }

    fun delete(songId: SongId) {
        dao.deleteBySongId(songId.id)
    }

    fun deleteByIds(songIds: List<SongId>) {
        dao.deleteBySongIds(songIds.map { it.id })
    }
}
