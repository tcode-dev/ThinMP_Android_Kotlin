package dev.tcode.thinmp.model.media

import android.net.Uri
import dev.tcode.thinmp.model.media.valueObject.AlbumId

data class AlbumDetailModel(
    var id: AlbumId,
    var primaryText: String,
    var secondaryText: String,
    var imageUri: Uri,
    var songs: List<SongModel> = emptyList()
)