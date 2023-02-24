package dev.tcode.thinmp.model.media

import dev.tcode.thinmp.model.media.valueObject.PlaylistId

data class PlaylistModel(
    var id: PlaylistId,
    var primaryText: String,
)