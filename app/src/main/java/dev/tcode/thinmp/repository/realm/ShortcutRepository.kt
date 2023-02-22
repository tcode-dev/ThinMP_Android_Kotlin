package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.realm.FavoriteArtistRealmModel
import dev.tcode.thinmp.model.realm.ShortcutRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults

enum class ItemType {
    ARTIST, ALBUM, PLAYLIST
}

class ShortcutRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(ShortcutRealmModel::class))

        realm = Realm.open(config)
    }

    fun existsArtist(artistId: ArtistId): Boolean {
        return exists(artistId.id, ItemType.ARTIST)
    }

    fun existsAlbum(albumId: AlbumId): Boolean {
        return exists(albumId.id, ItemType.ALBUM)
    }

    fun existsPlaylist(playlistId: PlaylistId): Boolean {
        return exists(playlistId.id, ItemType.PLAYLIST)
    }

    fun addArtist(artistId: ArtistId) {
        add(artistId.id, ItemType.ARTIST)
    }

    fun addAlbum(albumId: AlbumId) {
        add(albumId.id, ItemType.ALBUM)
    }

    fun addPlaylist(playlistId: PlaylistId) {
        add(playlistId.id, ItemType.PLAYLIST)
    }

    fun deleteArtist(artistId: ArtistId) {
        delete(artistId.id, ItemType.ARTIST)
    }

    fun deleteAlbum(albumId: AlbumId) {
        delete(albumId.id, ItemType.ALBUM)
    }

    fun deletePlaylist(playlistId: PlaylistId) {
        delete(playlistId.id, ItemType.PLAYLIST)
    }

    private fun exists(itemId: String, itemType: ItemType): Boolean {
        return find(itemId, itemType).isNotEmpty()
    }

    private fun find(itemId: String, itemType: ItemType): RealmResults<ShortcutRealmModel> {
        return realm.query<ShortcutRealmModel>("itemId == $0 AND type = $1", itemId, itemType.ordinal).find()
    }

    private fun add(_itemId: String, itemType: ItemType) {
        realm.writeBlocking {
            copyToRealm(ShortcutRealmModel().apply {
                itemId = _itemId
                type = itemType.ordinal
            })
        }
    }

    private fun delete(itemId: String, itemType: ItemType) {
        realm.writeBlocking {
            val shortcut = find(itemId, itemType).first()

            findLatest(shortcut)?.let { delete(it) }
        }
    }
}