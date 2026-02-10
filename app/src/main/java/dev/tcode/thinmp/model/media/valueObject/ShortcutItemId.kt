package dev.tcode.thinmp.model.media.valueObject

import dev.tcode.thinmp.repository.room.ItemType

interface ShortcutItemId {
    fun toId(type: ItemType): String {
        return when (type) {
            ItemType.ARTIST -> (this as ArtistId).id
            ItemType.ALBUM -> (this as AlbumId).id
            ItemType.PLAYLIST -> (this as PlaylistId).id
        }
    }
}