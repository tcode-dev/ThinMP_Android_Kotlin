package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.realm.PlaylistRepository

interface PlaylistRegister {
    fun createPlaylist(songId: SongId, text: String) {
        val repository = PlaylistRepository()

        repository.create(songId, text)
    }

    fun addPlaylist(playlistId: PlaylistId, songId: SongId) {
        val repository = PlaylistRepository()

        repository.add(playlistId, songId)
    }

    fun deletePlaylist(playlistId: PlaylistId) {
        val repository = PlaylistRepository()

        repository.delete(playlistId)
    }

    fun update(playlistId: PlaylistId, name: String, songIds: List<SongId>) {
        val repository = PlaylistRepository()

        repository.update(playlistId, name, songIds)
    }
}