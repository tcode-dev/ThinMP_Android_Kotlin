package dev.tcode.thinmp.model.media

import android.net.Uri
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId

data class ShortcutModel(
    var itemId: ShortcutItemId, var primaryText: String, var secondaryText: String, var imageUri: Uri
) {
    val url: String
        get() {
            return when (this.itemId) {
                is ArtistId -> "${NavConstant.ARTIST_DETAIL}/${(this.itemId as ArtistId).id}"
                is AlbumId -> "${NavConstant.ALBUM_DETAIL}/${(this.itemId as AlbumId).id}"
                is PlaylistId -> "${NavConstant.PLAYLIST_DETAIL}/${(this.itemId as PlaylistId).id}"
                else -> throw IllegalArgumentException("Unknown expression")
            }
        }
}