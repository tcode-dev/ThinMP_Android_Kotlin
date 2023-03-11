package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.repository.media.AlbumRepository
import dev.tcode.thinmp.repository.media.ArtistRepository
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.repository.realm.ItemType
import dev.tcode.thinmp.repository.realm.PlaylistRepository
import dev.tcode.thinmp.repository.realm.ShortcutRepository

class ShortcutService(val context: Context) {
    fun findAll(): List<ShortcutModel> {
        val resources = context.resources
        val shortcutRepository = ShortcutRepository()
        val artistRepository = ArtistRepository(context)
        val albumRepository = AlbumRepository(context)
        val playlistRepository = PlaylistRepository()
        val songRepository = SongRepository(context)
        val shortcuts = shortcutRepository.findAll()
        val group = shortcuts.groupBy { it.type }
        var shortcutArtists: List<ShortcutModel> = emptyList()
        var shortcutAlbums: List<ShortcutModel> = emptyList()
        var shortcutPlaylists: List<ShortcutModel> = emptyList()

        if (group.containsKey(ItemType.ARTIST.ordinal)) {
            val artistIds = group[ItemType.ARTIST.ordinal]?.map { ArtistId(it.itemId) }!!
            val artists = artistRepository.findByIds(artistIds)

            shortcutArtists = artists.map {
                val albums = albumRepository.findByArtistId(it.id)

                ShortcutModel(it.artistId, it.name, resources.getString(R.string.artist), albums.first().getImageUri(), ItemType.ARTIST)
            }
        }

        if (group.containsKey(ItemType.ALBUM.ordinal)) {
            val albumIds = group[ItemType.ALBUM.ordinal]?.map { AlbumId(it.itemId) }!!
            val albums = albumRepository.findByIds(albumIds)

            shortcutAlbums = albums.map {
                ShortcutModel(it.albumId, it.name, resources.getString(R.string.album), it.getImageUri(), ItemType.ALBUM)
            }
        }

        if (group.containsKey(ItemType.PLAYLIST.ordinal)) {
            val playlistIds = group[ItemType.PLAYLIST.ordinal]?.map { PlaylistId(it.itemId) }!!
            val playlists = playlistRepository.findByIds(playlistIds)

            shortcutPlaylists = playlists.map {
                val song = songRepository.findById(it.songs.first().songId)

                ShortcutModel(PlaylistId(it.id), it.name, resources.getString(R.string.playlist), song!!.getImageUri(), ItemType.PLAYLIST)
            }
        }

        return shortcuts.sortedByDescending { it.order }.mapNotNull { shortcut ->
            when (shortcut.type) {
                ItemType.ARTIST.ordinal -> shortcutArtists.first { artist -> artist.itemId == ArtistId(shortcut.itemId) }
                ItemType.ALBUM.ordinal -> shortcutAlbums.find { album -> album.itemId == AlbumId(shortcut.itemId) }
                ItemType.PLAYLIST.ordinal -> shortcutPlaylists.find { playlist -> playlist.itemId == PlaylistId(shortcut.itemId) }
                else -> {
                    throw IllegalArgumentException("Unknown expression")
                }
            }
        }
    }
}