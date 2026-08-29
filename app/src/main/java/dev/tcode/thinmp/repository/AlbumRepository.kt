package dev.tcode.thinmp.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId

class AlbumRepository(context: Context) : MediaStoreRepository<AlbumModel>(
    context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Albums.ARTIST)
) {
    suspend fun findAll(): List<AlbumModel> {
        return getList(sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC")
    }

    suspend fun findById(albumId: String): AlbumModel? {
        return get(MediaStore.Audio.Albums._ID + " = ?", arrayOf(albumId))
    }

    suspend fun findByIds(albumIds: List<AlbumId>): List<AlbumModel> {
        return getListByIds(MediaStore.Audio.Albums._ID, albumIds.map { it.id })
    }

    suspend fun findByArtistId(artistId: String): List<AlbumModel> {
        return getList(
            MediaStore.Audio.Media.ARTIST_ID + " = ?",
            arrayOf(artistId),
            "${MediaStore.Audio.Media.ALBUM} ASC"
        )
    }

    private fun getId(cursor: Cursor): AlbumId {
        val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)) ?: ""

        return AlbumId(id)
    }

    private fun getArtistId(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)) ?: ""
    }

    private fun getArtistName(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: ""
    }

    private fun getAlbumName(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)) ?: ""
    }

    private fun getAlbum(cursor: Cursor): AlbumModel {
        return AlbumModel(
            getId(cursor),
            getAlbumName(cursor),
            getArtistId(cursor),
            getArtistName(cursor),
        )
    }

    override fun fetch(cursor: Cursor): AlbumModel {
        return getAlbum(cursor)
    }
}
