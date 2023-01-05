package dev.tcode.thinmp.model.media

import android.net.Uri

class AlbumModel(
    public override var id: String,
    public override var name: String,
    val artistId: String,
    val artistName: String
): Music() {
    override fun getTrackList(): List<SongModel> {
        val trackList: MutableList<SongModel> = ArrayList()

//        trackList.add(this)

        return trackList
    }

    fun getImageUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/${id}")
    }
}