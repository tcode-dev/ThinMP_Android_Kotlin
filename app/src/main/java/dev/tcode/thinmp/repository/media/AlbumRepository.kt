package dev.tcode.thinmp.repository.media

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId

class AlbumRepository(context: Context) : MediaStoreRepository<AlbumModel>(
    context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Albums.ARTIST)
) {
    fun findAll(): List<AlbumModel> {
        selection = null
        selectionArgs = null
        sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"
        bundle = null

        return getList();
    }

    fun findById(albumId: String): AlbumModel? {
        selection = MediaStore.Audio.Albums._ID + " = ?"
        selectionArgs = arrayOf(albumId)
        sortOrder = null
        bundle = null

        return get()
    }

    fun findByIds(albumIds: List<AlbumId>): List<AlbumModel> {
        val ids = albumIds.map { it.id }

        selection = MediaStore.Audio.Albums._ID + " IN (" + makePlaceholders(ids.size) + ")"
        selectionArgs = toStringArray(ids)
        sortOrder = null
        bundle = null

        return getList()
    }

    fun findByArtistId(artistId: String): List<AlbumModel> {
        selection = MediaStore.Audio.Media.ARTIST_ID + " = ?"
        selectionArgs = arrayOf(artistId)
        sortOrder = "${MediaStore.Audio.Media.ALBUM} ASC"
        bundle = null

        return getList()
    }

    fun findRecentlyAdded(limit: Int): List<AlbumModel> {
        selection = null
        selectionArgs = null
        sortOrder = null
        bundle = Bundle().apply {
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Audio.Artists._ID))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        }

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