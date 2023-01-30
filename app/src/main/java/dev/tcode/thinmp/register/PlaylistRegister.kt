package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.realm.PlaylistRepository

interface PlaylistRegister {
    fun createPlaylist(songId: SongId, text: String) {
        val repository = PlaylistRepository()

        repository.create(songId, text)
    }
}