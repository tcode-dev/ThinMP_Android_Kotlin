package dev.tcode.thinmp.model.media

import android.net.Uri
import dev.tcode.thinmp.model.media.valueObject.PlaylistId

data class PlaylistDetailModel(
    var id: PlaylistId,
    var primaryText: String,
    var secondaryText: String,
    var imageUri: Uri,
    var songs: List<SongModel> = emptyList()
)