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
 * Everything a SongModel holds travels with the item, so whoever reads the queue - the service, or a
 * MediaController in PlaybackController - gets the whole song back from it without a MediaStore
 * query. The duration is MediaStore's own value, 0 for an untagged file, rather than the player's.
 *
 * Without [withUri] the item carries only the song's identity and metadata, which is all a
 * controller has to send: the service puts the URI back in onAddMediaItems().
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

/** Null for an item that did not come from [toMediaItem]. */
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

/** The same URI SongModel.getMediaUri() builds, from the media id alone. */
fun songMediaUri(mediaId: String): Uri {
    return Uri.parse("${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/${mediaId}")
}
