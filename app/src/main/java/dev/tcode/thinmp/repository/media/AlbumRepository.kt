package dev.tcode.thinmp.repository.media

import android.content.Context
import android.provider.MediaStore
import dev.tcode.thinmp.model.AlbumModel

class AlbumRepository(context: Context) : MediaStoreRepository<AlbumModel>(
    context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf(
        MediaStore.Audio.Albums._ID,
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Albums.ARTIST
    )
) {
    fun findAll(): List<AlbumModel> {
        selection = null
        selectionArgs = null
        sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"

        return getList();
    }

    private fun getId(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Albums._ID)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getArtistId(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
            ?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getArtistName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getAlbumName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ALBUM)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getAlbum(): AlbumModel {
        return AlbumModel(
            getId(),
            getAlbumName(),
            getArtistId(),
            getArtistName(),
        )
    }

    override fun fetch(): AlbumModel {
        return getAlbum()
    }
}