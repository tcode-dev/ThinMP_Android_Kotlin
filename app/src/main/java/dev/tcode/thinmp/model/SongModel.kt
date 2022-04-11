package dev.tcode.thinmp.model

import android.net.Uri

class SongModel(
    public override var id: String,
    public override var name: String,
    val artistId: String,
    val artistName: String,
    val albumId: String,
    val albumName: String,
    val duration: Int?
): Music() {
    override fun getTrackList(): List<SongModel> {
        val trackList: MutableList<SongModel> = ArrayList()

        trackList.add(this)

        return trackList
    }

    override var secondaryText: String? = null
        get() = artistName

    override fun getUri(): Uri? {
        return Uri.parse("content://media/external/audio/albumart/${albumId}")
    }
}
