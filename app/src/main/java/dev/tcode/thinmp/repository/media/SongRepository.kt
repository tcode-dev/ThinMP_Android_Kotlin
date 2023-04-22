package dev.tcode.thinmp.repository.media

import android.content.Context
import android.provider.MediaStore
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId

class SongRepository(context: Context) : MediaStoreRepository<SongModel>(
    context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION
    )
) {
    fun findById(songId: String): SongModel? {
        selection = MediaStore.Audio.Media._ID + " = ?"
        selectionArgs = arrayOf(songId)
        sortOrder = null

        return get()
    }

    fun findByIds(songIds: List<SongId>): List<SongModel> {
        val ids = songIds.map { it.id }

        selection = MediaStore.Audio.Media._ID + " IN (" + makePlaceholders(ids.size) + ") " + "AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1"
        selectionArgs = toStringArray(ids)
        sortOrder = null

        return getList()
    }

    fun findByArtistId(artistId: String): List<SongModel> {
        selection = MediaStore.Audio.Media.ARTIST_ID + " = ? AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1"
        selectionArgs = arrayOf(artistId)
        sortOrder = MediaStore.Audio.Media.ALBUM + " ASC, " + MediaStore.Audio.Media._ID + " ASC"
        return getList()
    }

    fun findByAlbumId(albumId: String): List<SongModel> {
        selection = MediaStore.Audio.Media.ALBUM_ID + " = ? AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1"
        selectionArgs = arrayOf(albumId)
        sortOrder = MediaStore.Audio.Media._ID + " ASC"

        return getList()
    }

    fun findAll(): List<SongModel> {
        selection = MediaStore.Audio.Media.IS_MUSIC + " = 1"
        selectionArgs = null
        sortOrder = (MediaStore.Audio.Media.ARTIST + " ASC, " + MediaStore.Audio.Media.ALBUM + " ASC, " + MediaStore.Audio.Media._ID + " ASC")
        return getList()
    }

    private fun getId(): SongId {
        val id = cursor?.getColumnIndex(MediaStore.Audio.Media._ID)?.let { cursor?.getString(it) } ?: ""

        return SongId(id)
    }

    private fun getTitle(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.TITLE)?.let { cursor?.getString(it) } ?: ""
    }

    private fun getArtistId(): ArtistId {
        val id = cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)?.let { cursor?.getString(it) } ?: ""

        return ArtistId(id)
    }

    private fun getArtistName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST)?.let { cursor?.getString(it) } ?: ""
    }

    private fun getAlbumId(): AlbumId {
        val id = cursor?.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)?.let { cursor?.getString(it) } ?: ""

        return AlbumId(id)
    }

    private fun getAlbumName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ALBUM)?.let { cursor?.getString(it) } ?: ""
    }

    private fun getDuration(): Int {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.DURATION)?.let { cursor?.getInt(it) } ?: 0
    }

    private fun getSong(): SongModel {
        return SongModel(
            getId(), getTitle(), getArtistId(), getArtistName(), getAlbumId(), getAlbumName(), getDuration()
        )
    }

    override fun fetch(): SongModel {
        return getSong()
    }
}