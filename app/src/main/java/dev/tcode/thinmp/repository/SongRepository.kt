package dev.tcode.thinmp.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
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
        return get(MediaStore.Audio.Media._ID + " = ?", arrayOf(songId))
    }

    suspend fun findByIds(songIds: List<SongId>): List<SongModel> {
        return getListByIds(MediaStore.Audio.Media._ID, songIds.map { it.id }, MediaStore.Audio.Media.IS_MUSIC + " = 1")
    }

    suspend fun findByArtistId(artistId: String): List<SongModel> {
        return getList(
            MediaStore.Audio.Media.ARTIST_ID + " = ? AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1",
            arrayOf(artistId),
            "${MediaStore.Audio.Media.ALBUM} ASC, $trackNumberSortOrder"
        )
    }

    suspend fun findByAlbumId(albumId: String): List<SongModel> {
        return getList(
            MediaStore.Audio.Media.ALBUM_ID + " = ? AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1",
            arrayOf(albumId),
            trackNumberSortOrder
        )
    }

    /**
     * 最近追加された曲を含むアルバムから順に、アルバムごとに1曲ずつ、最大[limit]件返す。
     * アルバムのテーブルには追加日時がないので、曲の追加日時で判断する。
     *
     * グループ化と件数の制限はSQLで行う(MediaStoreはQUERY_ARG_SQL_GROUP_BYに対応している)ので、
     * 全曲を取得してから絞り込む必要はない。
     *
     * GROUP BYで素のdate_addedを使うと、グループ内のどの行の値になるかは不定なので、
     * MAX()でアルバム内で最も新しい曲の日時を使う。
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
        return getList(
            MediaStore.Audio.Media.IS_MUSIC + " = 1",
            null,
            MediaStore.Audio.Media.TITLE + " ASC"
        )
    }

    private fun getId(cursor: Cursor): SongId {
        val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) ?: ""

        return SongId(id)
    }

    private fun getTitle(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: ""
    }

    private fun getArtistId(cursor: Cursor): ArtistId {
        val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)) ?: ""

        return ArtistId(id)
    }

    private fun getArtistName(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: ""
    }

    private fun getAlbumId(cursor: Cursor): AlbumId {
        val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)) ?: ""

        return AlbumId(id)
    }

    private fun getAlbumName(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) ?: ""
    }

    private fun getDuration(cursor: Cursor): Int {
        return cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
    }

    private fun getTrackNumber(cursor: Cursor): String {
        return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER)) ?: ""
    }

    private fun getSong(cursor: Cursor): SongModel {
        return SongModel(
            getId(cursor),
            getTitle(cursor),
            getArtistId(cursor),
            getArtistName(cursor),
            getAlbumId(cursor),
            getAlbumName(cursor),
            getDuration(cursor),
            getTrackNumber(cursor)
        )
    }

    override fun fetch(cursor: Cursor): SongModel {
        return getSong(cursor)
    }
}
