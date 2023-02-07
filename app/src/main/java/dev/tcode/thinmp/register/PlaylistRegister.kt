package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.realm.PlaylistRepository
import org.mongodb.kbson.BsonObjectId

interface PlaylistRegister {
    fun createPlaylist(songId: SongId, text: String) {
        val repository = PlaylistRepository()

        repository.create(songId, text)
    }

    fun addPlaylist(playlistId: BsonObjectId, songId: SongId) {
        val repository = PlaylistRepository()

        repository.add(playlistId, songId)
    }
}