package dev.tcode.thinmp.model.media

import android.net.Uri
import android.provider.MediaStore
import android.net.Uri.parse

class SongModel(
    public override var id: String,
    public override var name: String,
    val artistId: String,
    val artistName: String,
    val albumId: String,
    val albumName: String,
    val duration: Int
): Music() {
    override fun getTrackList(): List<SongModel> {
        val trackList: MutableList<SongModel> = ArrayList()

        trackList.add(this)

        return trackList
    }

    fun getImageUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/${albumId}")
    }

    fun getMediaUri(): Uri {
        return parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${id}")
    }
}
