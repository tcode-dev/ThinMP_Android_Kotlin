package dev.tcode.thinmp.model.media

import android.net.Uri

data class AlbumDetailModel (
    var id: String,
    var primaryText: String,
    var secondaryText: String,
    var imgUri: Uri,
    var songs: List<SongModel> = emptyList()
)