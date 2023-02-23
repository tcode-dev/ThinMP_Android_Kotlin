package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.repository.realm.ShortcutRepository

interface ShortcutRegister {
    fun existsShortcut(artistId: ArtistId): Boolean {
        val repository = ShortcutRepository()

        return repository.existsArtist(artistId)
    }

    fun existsShortcut(albumId: AlbumId): Boolean {
        val repository = ShortcutRepository()

        return repository.existsAlbum(albumId)
    }

    fun existsShortcut(playlistId: PlaylistId): Boolean {
        val repository = ShortcutRepository()

        return repository.existsPlaylist(playlistId)
    }

    fun addShortcut(artistId: ArtistId) {
        val repository = ShortcutRepository()

        repository.addArtist(artistId)
    }

    fun addShortcut(albumId: AlbumId) {
        val repository = ShortcutRepository()

        repository.addAlbum(albumId)
    }

    fun addShortcut(playlistId: PlaylistId) {
        val repository = ShortcutRepository()

        repository.addPlaylist(playlistId)
    }

    fun deleteShortcut(artistId: ArtistId) {
        val repository = ShortcutRepository()

        repository.deleteArtist(artistId)
    }

    fun deleteShortcut(albumId: AlbumId) {
        val repository = ShortcutRepository()

        repository.deleteAlbum(albumId)
    }

    fun deleteShortcut(playlistId: PlaylistId) {
        val repository = ShortcutRepository()

        repository.deletePlaylist(playlistId)
    }
}