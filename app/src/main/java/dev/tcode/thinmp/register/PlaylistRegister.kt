package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository

interface PlaylistRegister {
    suspend fun createPlaylist(songId: SongId, text: String) {
        val repository = PlaylistRepository()

        repository.create(songId, text)
    }

    /** false when the song is already in the playlist. */
    suspend fun addSongToPlaylist(playlistId: PlaylistId, songId: SongId): Boolean {
        val repository = PlaylistRepository()

        return repository.add(playlistId, songId)
    }

    suspend fun deletePlaylist(playlistId: PlaylistId) {
        val repository = PlaylistRepository()

        repository.delete(playlistId)
    }

    suspend fun updatePlaylist(playlistId: PlaylistId, name: String, songIds: List<SongId>) {
        val repository = PlaylistRepository()

        repository.updatePlaylist(playlistId, name, songIds)
    }

    suspend fun reorderPlaylists(playlistIds: List<PlaylistId>) {
        val repository = PlaylistRepository()

        repository.reorder(playlistIds)
    }
}