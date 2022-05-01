package dev.tcode.thinmp.repository.media

import android.content.Context
import android.provider.MediaStore
import dev.tcode.thinmp.model.media.ArtistModel

class ArtistRepository(context: Context) : MediaStoreRepository<ArtistModel>(
    context, MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, arrayOf(
        MediaStore.Audio.Artists._ID,
        MediaStore.Audio.Artists.ARTIST,
        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
        MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    )
) {
    fun findAll(): List<ArtistModel> {
        selection = null
        selectionArgs = null
        sortOrder = MediaStore.Audio.Artists.ARTIST + " ASC"

        return getList();
    }

    private fun getId(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Artists._ID)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getArtistName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getNumberOfAlbums(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getNumberOfTracks(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getArtist(): ArtistModel {
        return ArtistModel(
            getId(),
            getArtistName(),
            getNumberOfAlbums(),
            getNumberOfTracks(),
        )
    }

    override fun fetch(): ArtistModel {
        return getArtist()
    }
}