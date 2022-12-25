package dev.tcode.thinmp.model.media

import android.net.Uri

data class ArtistDetailModel (
    var id: String,
    var primaryText: String,
    var secondaryText: String,
    var imageUri: Uri,
    var albums: List<AlbumModel> = emptyList(),
    var songs: List<SongModel> = emptyList()
)
