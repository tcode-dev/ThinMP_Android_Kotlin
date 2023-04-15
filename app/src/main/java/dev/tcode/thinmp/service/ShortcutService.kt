package dev.tcode.thinmp.service

import android.content.Context
import android.content.res.Resources
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.realm.ShortcutRealmModel
import dev.tcode.thinmp.repository.media.AlbumRepository
import dev.tcode.thinmp.repository.media.ArtistRepository
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.repository.realm.ItemType
import dev.tcode.thinmp.repository.realm.PlaylistRepository
import dev.tcode.thinmp.repository.realm.ShortcutRepository

class ShortcutService(
    val context: Context,
    private val resources: Resources = context.resources,
    private val shortcutRepository: ShortcutRepository = ShortcutRepository(),
    private val artistRepository: ArtistRepository = ArtistRepository(context),
    private val albumRepository: AlbumRepository = AlbumRepository(context),
    private val songRepository: SongRepository = SongRepository(context),
    private val playlistRepository: PlaylistRepository = PlaylistRepository()
) {
    fun findAll(): List<ShortcutModel> {
        val shortcutRealmModels = shortcutRepository.findAll()
        val group = shortcutRealmModels.groupBy { it.type }
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

        val shortcutModels = shortcutRealmModels.mapNotNull { shortcut ->
            when (shortcut.type) {
                ItemType.ARTIST.ordinal -> shortcutArtists.firstOrNull { artist -> artist.itemId.toId(artist.type) == shortcut.itemId }
                ItemType.ALBUM.ordinal -> shortcutAlbums.firstOrNull { album -> album.itemId.toId(album.type) == shortcut.itemId }
                ItemType.PLAYLIST.ordinal -> shortcutPlaylists.firstOrNull { playlist -> playlist.itemId.toId(playlist.type) == shortcut.itemId }
                else -> {
                    throw IllegalArgumentException("Unknown expression")
                }
            }
        }

        if (!validation(shortcutRealmModels, shortcutModels)) {
            fix(shortcutRealmModels, shortcutModels)

            return findAll()
        }

        return shortcutModels
    }

    private fun validation(shortcutRealmModels: List<ShortcutRealmModel>, shortcutModels: List<ShortcutModel>): Boolean {
        return shortcutRealmModels.count() == shortcutModels.count()
    }

    private fun fix(shortcutRealmModels: List<ShortcutRealmModel>, shortcutModels: List<ShortcutModel>) {
        val deleteShortcutIds = shortcutRealmModels.filter { shortcutRealmModel ->
            shortcutModels.none { it.itemId.toId(it.type) == shortcutRealmModel.itemId }
        }.map { it.id }

        shortcutRepository.update(deleteShortcutIds)
    }
}