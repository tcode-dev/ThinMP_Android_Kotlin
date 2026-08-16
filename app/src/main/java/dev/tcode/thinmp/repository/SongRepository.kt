package dev.tcode.thinmp.repository

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
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
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.CD_TRACK_NUMBER
    )
) {
    private val trackNumberSortOrder = "CASE " +
            "WHEN ${MediaStore.Audio.Media.CD_TRACK_NUMBER} LIKE '%/%' THEN " +
            "CAST(SUBSTR(${MediaStore.Audio.Media.CD_TRACK_NUMBER}, 0, INSTR(${MediaStore.Audio.Media.CD_TRACK_NUMBER}, '/')) AS INTEGER) " +
            "ELSE " +
            "CAST(${MediaStore.Audio.Media.CD_TRACK_NUMBER} AS INTEGER) " +
            "END ASC"

    suspend fun findById(songId: String): SongModel? {
        selection = MediaStore.Audio.Media._ID + " = ?"
        selectionArgs = arrayOf(songId)
        sortOrder = null

        return get()
    }

    suspend fun findByIds(songIds: List<SongId>): List<SongModel> {
        return getListByIds(MediaStore.Audio.Media._ID, songIds.map { it.id }, MediaStore.Audio.Media.IS_MUSIC + " = 1")
    }

    suspend fun findByArtistId(artistId: String): List<SongModel> {
        selection = MediaStore.Audio.Media.ARTIST_ID + " = ? AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1"
        selectionArgs = arrayOf(artistId)
        sortOrder = "${MediaStore.Audio.Media.ALBUM} ASC, $trackNumberSortOrder"

        return getList()
    }

    suspend fun findByAlbumId(albumId: String): List<SongModel> {
        selection = MediaStore.Audio.Media.ALBUM_ID + " = ? AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1"
        selectionArgs = arrayOf(albumId)
        sortOrder = trackNumberSortOrder

        return getList()
    }

    /**
     * One track per album, the album with the most recently added track first, at most [limit] of
     * them. The albums collection has no date of its own - it carries numsongs, artist, the two
     * year columns and nothing else - so when an album was added is only knowable from its tracks.
     *
     * The grouping and the limit are SQL rather than Kotlin: MediaStore honours
     * QUERY_ARG_SQL_GROUP_BY, so this returns one row per album instead of every track on the
     * device for the main screen to whittle down.
     *
     * MAX(), because under a GROUP BY a bare date_added is taken from whichever row of the group
     * SQLite happened to keep, which for an album whose tracks were added at different times is
     * not the one that decides how recent the album is.
     */
    suspend fun findRecentlyAddedByAlbum(limit: Int): List<SongModel> {
        val bundle = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, MediaStore.Audio.Media.IS_MUSIC + " = 1")
            putString(ContentResolver.QUERY_ARG_SQL_GROUP_BY, MediaStore.Audio.Media.ALBUM_ID)
            putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "MAX(${MediaStore.Audio.Media.DATE_ADDED}) DESC")
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        }

        return getList(bundle)
    }

    suspend fun findAll(): List<SongModel> {
        selection = MediaStore.Audio.Media.IS_MUSIC + " = 1"
        selectionArgs = null
        sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

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

    private fun getTrackNumber(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER)?.let { cursor?.getString(it) } ?: ""
    }

    private fun getSong(): SongModel {
        return SongModel(
            getId(), getTitle(), getArtistId(), getArtistName(), getAlbumId(), getAlbumName(), getDuration(), getTrackNumber()
        )
    }

    override fun fetch(): SongModel {
        return getSong()
    }
}