package dev.tcode.thinmp.player

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId

private const val EXTRA_ARTIST_ID = "dev.tcode.thinmp.ARTIST_ID"
private const val EXTRA_ALBUM_ID = "dev.tcode.thinmp.ALBUM_ID"
private const val EXTRA_ALBUM_NAME = "dev.tcode.thinmp.ALBUM_NAME"
private const val EXTRA_DURATION = "dev.tcode.thinmp.DURATION"
private const val EXTRA_TRACK_NUMBER = "dev.tcode.thinmp.TRACK_NUMBER"

/**
 * SongModelの全項目をMediaItemに持たせるので、
 * キューを読む側(サービスやPlaybackController)はMediaStoreを引かずに曲を復元できる。
 * 長さはプレーヤーではなくMediaStoreの値で、タグがなければ0。
 *
 * [withUri]がfalseならURIを含めない。
 * コントローラーから送るときはこれで足り、URIはサービスがonAddMediaItems()で補う。
 */
fun SongModel.toMediaItem(withUri: Boolean): MediaItem {
    val extras = Bundle().apply {
        putString(EXTRA_ARTIST_ID, artistId.id)
        putString(EXTRA_ALBUM_ID, albumId.id)
        putString(EXTRA_ALBUM_NAME, albumName)
        putInt(EXTRA_DURATION, duration)
        putString(EXTRA_TRACK_NUMBER, trackNumber)
    }
    val metadata = MediaMetadata.Builder().setTitle(name).setArtist(artistName).setAlbumTitle(albumName).setArtworkUri(getImageUri()).setExtras(extras).build()
    val builder = MediaItem.Builder().setMediaId(id).setMediaMetadata(metadata)

    if (withUri) builder.setUri(getMediaUri())

    return builder.build()
}

/** [toMediaItem]で作ったものでなければnull。 */
fun MediaItem.toSongModel(): SongModel? {
    val extras = mediaMetadata.extras ?: return null

    return SongModel(
        SongId(mediaId),
        mediaMetadata.title?.toString() ?: "",
        ArtistId(extras.getString(EXTRA_ARTIST_ID) ?: ""),
        mediaMetadata.artist?.toString() ?: "",
        AlbumId(extras.getString(EXTRA_ALBUM_ID) ?: ""),
        extras.getString(EXTRA_ALBUM_NAME) ?: "",
        extras.getInt(EXTRA_DURATION),
        extras.getString(EXTRA_TRACK_NUMBER) ?: ""
    )
}

/** SongModel.getMediaUri()と同じURIを、media idだけから作る。 */
fun songMediaUri(mediaId: String): Uri {
    return Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${mediaId}")
}
