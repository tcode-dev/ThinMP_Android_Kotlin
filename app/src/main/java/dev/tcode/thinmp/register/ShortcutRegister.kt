package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.repository.realm.ShortcutRepository

interface ShortcutRegister {
    fun existsShortcutArtist(artistId: ArtistId): Boolean {
        val repository = ShortcutRepository()

        return repository.existsArtist(artistId)
    }

    fun existsShortcutAlbum(albumId: AlbumId): Boolean {
        val repository = ShortcutRepository()

        return repository.existsAlbum(albumId)
    }

    fun existsShortcutPlaylist(playlistId: PlaylistId): Boolean {
        val repository = ShortcutRepository()

        return repository.existsPlaylist(playlistId)
    }

    fun addShortcutArtist(artistId: ArtistId) {
        val repository = ShortcutRepository()

        repository.addArtist(artistId)
    }

    fun addShortcutAlbum(albumId: AlbumId) {
        val repository = ShortcutRepository()

        repository.addAlbum(albumId)
    }

    fun addShortcutPlaylist(playlistId: PlaylistId) {
        val repository = ShortcutRepository()

        repository.addPlaylist(playlistId)
    }
}