package dev.tcode.thinmp.repository.media

import android.content.Context
import android.provider.MediaStore
import dev.tcode.thinmp.model.media.ArtistModel
import dev.tcode.thinmp.model.media.valueObject.ArtistId

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

    fun findById(artistId: String): ArtistModel? {
        selection = MediaStore.Audio.Media._ID + " = ?"
        selectionArgs = arrayOf(artistId)
        sortOrder = null

        return get()
    }

    fun findByIds(artistIds: List<ArtistId>): List<ArtistModel> {
        val ids = artistIds.map { it.id }

        selection = MediaStore.Audio.Media._ID + " IN (" + makePlaceholders(ids.size) + ")"
        selectionArgs = toStringArray(ids)
        sortOrder = null

        return getList()
    }

    private fun getId(): ArtistId {
        val id = cursor?.getColumnIndex(MediaStore.Audio.Artists._ID)?.let { cursor?.getString(it) }
            ?: ""

        return ArtistId(id)
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