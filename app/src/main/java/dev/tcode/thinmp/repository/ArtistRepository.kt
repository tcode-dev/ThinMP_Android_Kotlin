package dev.tcode.thinmp.repository

import android.content.Context
import android.database.Cursor
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
        return getList(sortOrder = MediaStore.Audio.Artists.ARTIST + " ASC")
    }

    suspend fun findById(artistId: String): ArtistModel? {
        return get(MediaStore.Audio.Media._ID + " = ?", arrayOf(artistId))
    }

    suspend fun findByIds(artistIds: List<ArtistId>): List<ArtistModel> {
        return getListByIds(MediaStore.Audio.Media._ID, artistIds.map { it.id })
    }

    private fun getId(cursor: Cursor): ArtistId {
        val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists._ID)) ?: ""

        return ArtistId(id)
    }

    private fun getArtistName(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: ""
    }

    private fun getNumberOfAlbums(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)) ?: ""
    }

    private fun getNumberOfTracks(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)) ?: ""
    }

    private fun getArtist(cursor: Cursor): ArtistModel {
        return ArtistModel(
            getId(cursor),
            getArtistName(cursor),
            getNumberOfAlbums(cursor),
            getNumberOfTracks(cursor),
        )
    }

    override fun fetch(cursor: Cursor): ArtistModel {
        return getArtist(cursor)
    }
}
