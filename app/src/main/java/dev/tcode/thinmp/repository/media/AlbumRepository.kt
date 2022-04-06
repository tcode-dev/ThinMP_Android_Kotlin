package dev.tcode.thinmp.repository.media

import android.content.Context
import android.provider.MediaStore
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.model.SongModel

class AlbumRepository(context: Context) : MediaStoreRepository<AlbumModel>(
    context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
        MediaStore.Audio.Albums._ID,
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Albums.ARTIST
    )
) {
//    fun findById(songId: String): SongModel? {
//        selection = MediaStore.Audio.Media._ID + " = ?"
//        selectionArgs = arrayOf(songId)
//        sortOrder = null
//
//        return get()
//    }
//
//    fun findById(trackIdList: List<String>): List<SongModel> {
//        selection =
//            MediaStore.Audio.Media._ID + " IN (" + makePlaceholders(trackIdList.size) + ") " +
//                    "AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1"
//        selectionArgs = toStringArray(trackIdList)
//        sortOrder = null
//        return getList()
//    }
//
//    fun findByArtist(artistId: String): List<SongModel> {
//        selection =
//            MediaStore.Audio.Media.ARTIST_ID + " = ? AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1"
//        selectionArgs = arrayOf(artistId)
//        sortOrder = MediaStore.Audio.Media.ALBUM + " ASC, " + MediaStore.Audio.Media._ID + " ASC"
//        return getList()
//    }
//
//    fun findByAlbum(albumId: String): List<SongModel> {
//        selection =
//            MediaStore.Audio.Media.ALBUM_ID + " = ? AND " + MediaStore.Audio.Media.IS_MUSIC + " = 1"
//        selectionArgs = arrayOf(albumId)
//        sortOrder = MediaStore.Audio.Media._ID + " ASC"
//        return getList()
//    }

    fun findAll(): List<AlbumModel> {
//        selection = MediaStore.Audio.Media.IS_MUSIC + " = 1"
//        selectionArgs = null
//        sortOrder = (MediaStore.Audio.Media.ARTIST + " ASC, "
//                + MediaStore.Audio.Media.ALBUM + " ASC, "
//                + MediaStore.Audio.Media._ID + " ASC")
//        return getList()
        selection = null;
        selectionArgs = null;
        sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC";

        return getList();
    }

    private fun getId(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Albums._ID)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getArtistId(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
            ?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getArtistName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ARTIST)?.let { cursor?.getString(it) }
            ?: ""
    }

    private fun getAlbumName(): String {
        return cursor?.getColumnIndex(MediaStore.Audio.Media.ALBUM)?.let { cursor?.getString(it) }
            ?: ""
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