package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.repository.realm.ShortcutRepository

interface ShortcutRegister {
    fun addShortcutArtist(artistId: ArtistId) {
        val repository = ShortcutRepository()

        repository.addArtist(artistId)
    }

    fun addShortcutArtist(albumId: AlbumId) {
        val repository = ShortcutRepository()

        repository.addAlbum(albumId)
    }

    fun addShortcutArtist(playlistId: PlaylistId) {
        val repository = ShortcutRepository()

        repository.addPlaylist(playlistId)
    }
}