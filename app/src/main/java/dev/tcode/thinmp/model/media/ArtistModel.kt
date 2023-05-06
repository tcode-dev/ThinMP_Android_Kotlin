package dev.tcode.thinmp.model.media

import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.model.media.valueObject.ArtistId

class ArtistModel(
    val artistId: ArtistId,
    public override var name: String,
    public var numberOfAlbums: String,
    public var numberOfTracks: String,
): Music() {
    override var id: String = ""
        get() = artistId.id

    val url: String
        get() =  "${NavConstant.ARTIST_DETAIL}/${this.id}"
}