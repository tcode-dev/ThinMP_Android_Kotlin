package dev.tcode.thinmp.repository

import android.content.Context
import android.provider.MediaStore
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId

class AlbumRepository(context: Context) : MediaStoreRepository<AlbumModel>(
    context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Albums.ARTIST)
) {
    suspend fun findAll(): List<AlbumModel> {
        selection = null
        selectionArgs = null
        sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"

        return getList();
    }

    suspend fun findById(albumId: String): AlbumModel? {
        selection = MediaStore.Audio.Albums._ID + " = ?"
        selectionArgs = arrayOf(albumId)
        sortOrder = null

        return get()
    }

    suspend fun findByIds(albumIds: List<AlbumId>): List<AlbumModel> {
        return getListByIds(MediaStore.Audio.Albums._ID, albumIds.map { it.id })
    }

    suspend fun findByArtistId(artistId: String): List<AlbumModel> {
        selection = MediaStore.Audio.Media.ARTIST_ID + " = ?"
        selectionArgs = arrayOf(artistId)
        sortOrder = "${MediaStore.Audio.Media.ALBUM} ASC"

        return getList()
    }

    private fun getId(): AlbumId {
        val id = cursor?.getColumnIndex(MediaStore.Audio.Albums._ID)?.let { cursor?.getString(it) } ?: ""

        return AlbumId(id)
    }

    private fun getArtistId(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)?.let { cursor?.getString(it) } ?: ""
    }

    private fun getArtistName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST)?.let { cursor?.getString(it) } ?: ""
    }

    private fun getAlbumName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ALBUM)?.let { cursor?.getString(it) } ?: ""
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