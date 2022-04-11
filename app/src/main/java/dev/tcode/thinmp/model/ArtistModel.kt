package dev.tcode.thinmp.model

import android.net.Uri

class ArtistModel(
    public override var id: String,
    public override var name: String,
    public var numberOfAlbums: String,
    public var numberOfTracks: String,
): Music() {
    override fun getTrackList(): List<SongModel> {
        val trackList: MutableList<SongModel> = ArrayList()

//        trackList.add(this)

        return trackList
    }

    override fun getUri(): Uri? {
        return null
    }

    override var secondaryText: String? = null


}