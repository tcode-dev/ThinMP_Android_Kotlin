package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository

class PlaylistsService(val context: Context) {
    suspend fun findAll(): List<PlaylistModel> {
        val repository = PlaylistRepository()
        val playlists = repository.findAll()

        return playlists.map { PlaylistModel(PlaylistId(it.id), it.name) }
    }

    /** The playlists the song is already in, which the register popup shows as registered. */
    suspend fun findRegisteredPlaylistIds(songId: SongId): Set<PlaylistId> {
        val repository = PlaylistRepository()

        return repository.findPlaylistIdsBySongId(songId).toSet()
    }
}