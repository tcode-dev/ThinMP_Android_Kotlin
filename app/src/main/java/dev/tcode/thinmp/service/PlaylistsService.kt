package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.repository.room.PlaylistRepository

class PlaylistsService(val context: Context) {
    fun findAll(): List<PlaylistModel> {
        val repository = PlaylistRepository()
        val playlists = repository.findAll()

        return playlists.map { PlaylistModel(PlaylistId(it.id), it.name) }
    }
}