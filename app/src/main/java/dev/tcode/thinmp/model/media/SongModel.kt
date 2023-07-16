package dev.tcode.thinmp.model.media

import android.net.Uri
import android.provider.MediaStore
import dev.tcode.thinmp.constant.MediaConstant
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId

class SongModel(
    val songId: SongId,
    public override var name: String,
    val artistId: ArtistId,
    val artistName: String,
    val albumId: AlbumId,
    val albumName: String,
    val duration: Int,
    val trackNumber: String
): Music() {
    override var id: String = ""
        get() = songId.id

    fun getImageUri(): Uri {
        return Uri.parse("${MediaConstant.ALBUM_ART}/${albumId.id}")
    }

    fun getMediaUri(): Uri {
        return Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${id}")
    }

    fun getTrackNumber(): Int {
        // "15"、"15/30" → 15
        val regex = Regex("""\d{1,}""")
        val match = regex.find(trackNumber) ?: return 0

        return match.value.toInt()
    }
}