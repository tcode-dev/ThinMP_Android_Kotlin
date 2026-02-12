package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.room.PlaylistRepository

interface PlaylistRegister {
    fun createPlaylist(songId: SongId, text: String) {
        val repository = PlaylistRepository()

        repository.create(songId, text)
    }

    fun addSong(playlistId: PlaylistId, songId: SongId) {
        val repository = PlaylistRepository()

        repository.add(playlistId, songId)
    }

    fun deletePlaylist(playlistId: PlaylistId) {
        val repository = PlaylistRepository()

        repository.delete(playlistId)
    }

    fun updatePlaylist(playlistId: PlaylistId, name: String, songIds: List<SongId>) {
        val repository = PlaylistRepository()

        repository.updatePlaylist(playlistId, name, songIds)
    }

    fun updatePlaylists(playlistIds: List<PlaylistId>) {
        val repository = PlaylistRepository()

        repository.updatePlaylists(playlistIds)
    }
}