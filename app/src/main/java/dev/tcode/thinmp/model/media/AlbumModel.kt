package dev.tcode.thinmp.model.media

import android.net.Uri
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.model.media.valueObject.AlbumId

class AlbumModel(
    val albumId: AlbumId,
    public override var name: String,
    val artistId: String,
    val artistName: String
): Music() {
    override var id: String = ""
        get() = albumId.id

    override fun getTrackList(): List<SongModel> {
        val trackList: MutableList<SongModel> = ArrayList()

//        trackList.add(this)

        return trackList
    }

    val url: String
        get() =  "${NavConstant.ALBUM_DETAIL}/${this.id}"

    fun getImageUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/${id}")
    }
}