package dev.tcode.thinmp.model.media

import android.net.Uri
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.repository.realm.ItemType

data class ShortcutModel(
    var itemId: ShortcutItemId, var primaryText: String, var secondaryText: String, var imageUri: Uri, var type: ItemType
) {
    val url: String
        get() {
            return when (this.type) {
                ItemType.ARTIST -> "${NavConstant.ARTIST_DETAIL}/${(this.itemId as ArtistId).id}"
                ItemType.ALBUM -> "${NavConstant.ALBUM_DETAIL}/${(this.itemId as AlbumId).id}"
                ItemType.PLAYLIST -> "${NavConstant.PLAYLIST_DETAIL}/${(this.itemId as PlaylistId).id}"
            }
        }
}