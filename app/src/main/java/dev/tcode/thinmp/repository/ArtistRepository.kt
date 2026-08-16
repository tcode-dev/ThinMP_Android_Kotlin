package dev.tcode.thinmp.repository

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
    suspend fun findAll(): List<ArtistModel> {
        selection = null
        selectionArgs = null
        sortOrder = MediaStore.Audio.Artists.ARTIST + " ASC"

        return getList();
    }

    suspend fun findById(artistId: String): ArtistModel? {
        selection = MediaStore.Audio.Media._ID + " = ?"
        selectionArgs = arrayOf(artistId)
        sortOrder = null

        return get()
    }

    suspend fun findByIds(artistIds: List<ArtistId>): List<ArtistModel> {
        return getListByIds(MediaStore.Audio.Media._ID, artistIds.map { it.id })
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