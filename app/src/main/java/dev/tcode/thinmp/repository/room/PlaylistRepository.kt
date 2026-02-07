package dev.tcode.thinmp.repository.room

import dev.tcode.thinmp.application.MainApplication
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.room.PlaylistEntity
import dev.tcode.thinmp.model.room.PlaylistSongEntity

class PlaylistRepository {
    private val db = AppDatabase.getDatabase(MainApplication.appContext)
    private val playlistDao = db.playlistDao()
    private val playlistSongDao = db.playlistSongDao()

    fun create(songId: SongId, name: String) {
        val playlist = PlaylistEntity(name = name, order = increment())
        val song = PlaylistSongEntity(playlistId = playlist.id, songId = songId.id)

        playlistDao.insert(playlist)
        playlistSongDao.insert(song)
    }

    fun add(playlistId: PlaylistId, songId: SongId) {
        val song = PlaylistSongEntity(playlistId = playlistId.id, songId = songId.id)
        playlistSongDao.insert(song)
    }

    fun delete(playlistId: PlaylistId, songIds: List<SongId>) {
        playlistSongDao.deleteByPlaylistIdAndSongIds(playlistId.id, songIds.map { it.id })
    }

    fun updatePlaylist(playlistId: PlaylistId, name: String, songIds: List<SongId>) {
        val playlist = playlistDao.findById(playlistId.id) ?: return

        playlistDao.update(playlist.copy(name = name))
        playlistSongDao.deleteByPlaylistId(playlistId.id)
        val songs = songIds.map { songId ->
            PlaylistSongEntity(playlistId = playlistId.id, songId = songId.id)
        }
        playlistSongDao.insertAll(songs)
    }

    fun updatePlaylists(playlistIds: List<PlaylistId>) {
        val playlists = findAll()
        val group = playlists.groupBy { playlist -> playlistIds.any { it.id == playlist.id } }
        val deletePlaylists = group[false] ?: emptyList()
        val sortedPlaylists = if (group[true] != null) {
            playlistIds.mapNotNull { playlistId -> group[true]?.first { it.id == playlistId.id } }
        } else {
            emptyList()
        }

        deletePlaylists.forEach { playlist ->
            playlistSongDao.deleteByPlaylistId(playlist.id)
            playlistDao.deleteById(playlist.id)
        }

        for ((index, playlist) in sortedPlaylists.withIndex()) {
            playlistDao.update(playlist.copy(order = index + 1))
        }
    }

    fun findAll(): List<PlaylistEntity> {
        return playlistDao.findAll()
    }

    fun findById(playlistId: PlaylistId): PlaylistEntity? {
        return playlistDao.findById(playlistId.id)
    }

    fun findByIds(playlistIds: List<PlaylistId>): List<PlaylistEntity> {
        return playlistDao.findByIds(playlistIds.map { it.id })
    }

    fun findSongsByPlaylistId(playlistId: PlaylistId): List<PlaylistSongEntity> {
        return playlistSongDao.findByPlaylistId(playlistId.id)
    }

    fun delete(playlistId: PlaylistId) {
        playlistSongDao.deleteByPlaylistId(playlistId.id)
        playlistDao.deleteById(playlistId.id)
    }

    private fun increment(): Int {
        return playlistDao.getMaxOrder() + 1
    }
}
