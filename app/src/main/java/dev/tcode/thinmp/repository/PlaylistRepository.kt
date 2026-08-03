package dev.tcode.thinmp.repository

import androidx.room.withTransaction
import dev.tcode.thinmp.application.MainApplication
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.PlaylistEntity
import dev.tcode.thinmp.model.room.PlaylistSongEntity

class PlaylistRepository(
    private val db: AppDatabase = AppDatabase.getDatabase(MainApplication.appContext)
) {
    private val playlistDao = db.playlistDao()
    private val playlistSongDao = db.playlistSongDao()

    suspend fun create(songId: SongId, name: String) = db.withTransaction {
        val playlist = PlaylistEntity(name = name, order = increment())
        val song = PlaylistSongEntity(playlistId = playlist.id, songId = songId.id)

        playlistDao.insert(playlist)
        playlistSongDao.insert(song)
    }

    suspend fun add(playlistId: PlaylistId, songId: SongId) {
        val song = PlaylistSongEntity(playlistId = playlistId.id, songId = songId.id)
        playlistSongDao.insert(song)
    }

    suspend fun delete(playlistId: PlaylistId, songIds: List<SongId>) {
        playlistSongDao.deleteByPlaylistIdAndSongIds(playlistId.id, songIds.map { it.id })
    }

    suspend fun updatePlaylist(playlistId: PlaylistId, name: String, songIds: List<SongId>) = db.withTransaction {
        val playlist = playlistDao.findById(playlistId.id) ?: return@withTransaction

        playlistDao.update(playlist.copy(name = name))
        playlistSongDao.deleteByPlaylistId(playlistId.id)
        val songs = songIds.map { songId ->
            PlaylistSongEntity(playlistId = playlistId.id, songId = songId.id)
        }
        playlistSongDao.insertAll(songs)
    }

    suspend fun reorder(playlistIds: List<PlaylistId>) = db.withTransaction {
        val playlists = findAll()
        val group = playlists.groupBy { playlist -> playlistIds.any { it.id == playlist.id } }
        val deletePlaylists = group[false] ?: emptyList()
        // firstOrNull, which is what the surrounding mapNotNull was already written for: an id the
        // caller holds but the table no longer does is skipped rather than thrown out of the
        // transaction. first() made a caller working from a list that had gone stale crash instead.
        val sortedPlaylists = playlistIds.mapNotNull { playlistId -> group[true]?.firstOrNull { it.id == playlistId.id } }

        deletePlaylists.forEach { playlist ->
            playlistSongDao.deleteByPlaylistId(playlist.id)
            playlistDao.deleteById(playlist.id)
        }

        for ((index, playlist) in sortedPlaylists.withIndex()) {
            playlistDao.update(playlist.copy(order = index + 1))
        }
    }

    suspend fun findAll(): List<PlaylistEntity> {
        return playlistDao.findAll()
    }

    suspend fun findById(playlistId: PlaylistId): PlaylistEntity? {
        return playlistDao.findById(playlistId.id)
    }

    suspend fun findByIds(playlistIds: List<PlaylistId>): List<PlaylistEntity> {
        return playlistDao.findByIds(playlistIds.map { it.id })
    }

    suspend fun findSongsByPlaylistId(playlistId: PlaylistId): List<PlaylistSongEntity> {
        return playlistSongDao.findByPlaylistId(playlistId.id)
    }

    suspend fun delete(playlistId: PlaylistId) = db.withTransaction {
        playlistSongDao.deleteByPlaylistId(playlistId.id)
        playlistDao.deleteById(playlistId.id)
    }

    private suspend fun increment(): Int {
        return playlistDao.getMaxOrder() + 1
    }
}
