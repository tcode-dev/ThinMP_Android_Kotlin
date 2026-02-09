package dev.tcode.thinmp.service

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.ShortcutId
import dev.tcode.thinmp.model.room.ShortcutEntity
import dev.tcode.thinmp.repository.media.AlbumRepository
import dev.tcode.thinmp.repository.media.ArtistRepository
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.repository.room.ItemType
import dev.tcode.thinmp.repository.room.PlaylistRepository
import dev.tcode.thinmp.repository.room.ShortcutRepository

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
        val shortcutEntities = shortcutRepository.findAll()
        val group = shortcutEntities.groupBy { it.type }
        val map = mutableMapOf(ItemType.ARTIST.ordinal to emptyList<ShortcutModel>(), ItemType.ALBUM.ordinal to emptyList(), ItemType.PLAYLIST.ordinal to emptyList())

        if (group.containsKey(ItemType.ARTIST.ordinal)) {
            val artistIds = group[ItemType.ARTIST.ordinal]!!.map { ArtistId(it.itemId) }
            val artists = artistRepository.findByIds(artistIds)

            map[ItemType.ARTIST.ordinal] = artists.map { artist ->
                val albums = albumRepository.findByArtistId(artist.id)
                val sortedAlbums = albums.sortedBy { it.name }
                val imageUri = sortedAlbums.first().getImageUri()
                val id = group[ItemType.ARTIST.ordinal]!!.first { shortcut -> shortcut.itemId == artist.id }.id

                ShortcutModel(ShortcutId(id), artist.artistId, artist.name, resources.getString(R.string.artist), imageUri, ItemType.ARTIST)
            }
        }

        if (group.containsKey(ItemType.ALBUM.ordinal)) {
            val albumIds = group[ItemType.ALBUM.ordinal]!!.map { AlbumId(it.itemId) }
            val albums = albumRepository.findByIds(albumIds)

            map[ItemType.ALBUM.ordinal] = albums.map { album ->
                val id = group[ItemType.ALBUM.ordinal]!!.first { shortcut -> shortcut.itemId == album.id }.id

                ShortcutModel(ShortcutId(id), album.albumId, album.name, resources.getString(R.string.album), album.getImageUri(), ItemType.ALBUM)
            }
        }

        if (group.containsKey(ItemType.PLAYLIST.ordinal)) {
            val playlistIds = group[ItemType.PLAYLIST.ordinal]!!.map { PlaylistId(it.itemId) }
            val playlists = playlistRepository.findByIds(playlistIds)

            map[ItemType.PLAYLIST.ordinal] = playlists.map { playlist ->
                val playlistSongs = playlistRepository.findSongsByPlaylistId(PlaylistId(playlist.id))
                val firstSong = playlistSongs.firstOrNull()
                val songModel = firstSong?.let { songRepository.findById(it.songId) }
                val imageUri = songModel?.getImageUri() ?: Uri.EMPTY
                val id = group[ItemType.PLAYLIST.ordinal]!!.first { shortcut -> shortcut.itemId == playlist.id }.id

                ShortcutModel(ShortcutId(id), PlaylistId(playlist.id), playlist.name, resources.getString(R.string.playlist), imageUri, ItemType.PLAYLIST)
            }
        }

        val shortcutModels = shortcutEntities.mapNotNull { shortcut ->
            map[shortcut.type]?.firstOrNull { item -> item.itemId.toId(item.type) == shortcut.itemId }
        }

        if (!validation(shortcutEntities, shortcutModels)) {
            fix(shortcutEntities, shortcutModels)

            return findAll()
        }

        return shortcutModels
    }

    private fun validation(shortcutEntities: List<ShortcutEntity>, shortcutModels: List<ShortcutModel>): Boolean {
        return shortcutEntities.count() == shortcutModels.count()
    }

    private fun fix(shortcutEntities: List<ShortcutEntity>, shortcutModels: List<ShortcutModel>) {
        val deleteShortcutIds = shortcutEntities.filter { shortcutEntity ->
            shortcutModels.none { it.itemId.toId(it.type) == shortcutEntity.itemId }
        }.map { it.id }

        shortcutRepository.deleteByIds(deleteShortcutIds)
    }
}
