package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.realm.ShortcutRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

enum class ItemType {
    ARTIST, ALBUM, PLAYLIST
}

class ShortcutRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(ShortcutRealmModel::class))

        realm = Realm.open(config)
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

    private fun add(_itemId: String, itemType: ItemType) {
        realm.writeBlocking {
            copyToRealm(ShortcutRealmModel().apply {
                itemId = _itemId
                type = itemType.ordinal
            })
        }
    }
}