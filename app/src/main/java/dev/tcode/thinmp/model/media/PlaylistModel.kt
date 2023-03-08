package dev.tcode.thinmp.model.media

import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.model.media.valueObject.PlaylistId

data class PlaylistModel(
    var id: PlaylistId,
    var primaryText: String,
) {
    val url: String
        get() =  "${NavConstant.PLAYLIST_DETAIL}/${this.id}"
}