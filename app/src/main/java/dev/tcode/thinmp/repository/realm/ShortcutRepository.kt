package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.realm.FavoriteArtistRealmModel
import dev.tcode.thinmp.model.realm.ShortcutRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query

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

    private fun exists(itemId: String, itemType: ItemType): Boolean {
        return realm.query<ShortcutRealmModel>("itemId == $0 AND type = $1", itemId, itemType.ordinal).find().isNotEmpty()
    }

    private fun add(_itemId: String, itemType: ItemType) {
        realm.writeBlocking {
            copyToRealm(ShortcutRealmModel().apply {
                itemId = _itemId
                type = itemType.ordinal
            })
        }
    }
}