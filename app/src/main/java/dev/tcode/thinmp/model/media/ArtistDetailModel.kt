package dev.tcode.thinmp.model.media

import android.net.Uri
import dev.tcode.thinmp.model.media.valueObject.ArtistId

data class ArtistDetailModel (
    var id: ArtistId,
    var primaryText: String,
    var secondaryText: String,
    var imageUri: Uri,
    var albums: List<AlbumModel> = emptyList(),
    var songs: List<SongModel> = emptyList()
)