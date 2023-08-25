package dev.tcode.thinmp.service

import android.content.Context
import android.content.res.Resources
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.ShortcutId
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
        val map = mutableMapOf(ItemType.ARTIST.ordinal to emptyList<ShortcutModel>(), ItemType.ALBUM.ordinal to emptyList(), ItemType.PLAYLIST.ordinal to emptyList())

        if (group.containsKey(ItemType.ARTIST.ordinal)) {
            val artistIds = group[ItemType.ARTIST.ordinal]?.map { ArtistId(it.itemId) }!!
            val artists = artistRepository.findByIds(artistIds)

            map[ItemType.ARTIST.ordinal] = artists.map { artist ->
                val albums = albumRepository.findByArtistId(artist.id)
                val sortedAlbums = albums.sortedBy { it.name }
                val imageUri = sortedAlbums.first().getImageUri()
                val id = group[ItemType.ARTIST.ordinal]?.firstOrNull() { shortcut -> shortcut.itemId == artist.id }?.id

                ShortcutModel(ShortcutId(id ?: ""), artist.artistId, artist.name, resources.getString(R.string.artist), imageUri, ItemType.ARTIST)
            }
        }

        if (group.containsKey(ItemType.ALBUM.ordinal)) {
            val albumIds = group[ItemType.ALBUM.ordinal]?.map { AlbumId(it.itemId) }!!
            val albums = albumRepository.findByIds(albumIds)

            map[ItemType.ALBUM.ordinal] = albums.map { album ->
                val id = group[ItemType.ALBUM.ordinal]?.firstOrNull { shortcut -> shortcut.itemId == album.id }?.id

                ShortcutModel(ShortcutId(id ?: ""), album.albumId, album.name, resources.getString(R.string.album), album.getImageUri(), ItemType.ALBUM)
            }
        }

        if (group.containsKey(ItemType.PLAYLIST.ordinal)) {
            val playlistIds = group[ItemType.PLAYLIST.ordinal]?.map { PlaylistId(it.itemId) }!!
            val playlists = playlistRepository.findByIds(playlistIds)

            map[ItemType.PLAYLIST.ordinal] = playlists.map { playlist ->
                val imageUri = songRepository.findById(playlist.songs.first().songId)!!.getImageUri()
                val id = group[ItemType.PLAYLIST.ordinal]?.firstOrNull { shortcut -> shortcut.itemId == playlist.id }?.id

                ShortcutModel(ShortcutId(id ?: ""), PlaylistId(playlist.id), playlist.name, resources.getString(R.string.playlist), imageUri, ItemType.PLAYLIST)
            }
        }

        val shortcutModels = shortcutRealmModels.mapNotNull { shortcut ->
            map[shortcut.type]?.firstOrNull { item -> item.itemId.toId(item.type) == shortcut.itemId }
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

        shortcutRepository.deleteByIds(deleteShortcutIds)
    }
}