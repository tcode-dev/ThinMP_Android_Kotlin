package dev.tcode.thinmp.model

import android.net.Uri
import android.provider.MediaStore

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

    fun getUri(): Uri {
        return Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${albumId}")
    }
}
