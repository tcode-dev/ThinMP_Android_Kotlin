package dev.tcode.thinmp.model.media

import android.net.Uri
import dev.tcode.thinmp.model.Music
import dev.tcode.thinmp.model.SongModel

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

    fun getUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/${id}")
    }
}