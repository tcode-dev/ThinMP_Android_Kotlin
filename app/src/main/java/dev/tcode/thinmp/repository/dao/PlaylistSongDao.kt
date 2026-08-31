package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.tcode.thinmp.constant.SqliteConstant
import dev.tcode.thinmp.model.room.PlaylistSongEntity

@Dao
interface PlaylistSongDao {
    /** rowid is the insertion order, which is the order of the playlist. Without it the rows come
     * back in whatever order the index SQLite chose happens to hold them in. */
    @Query("SELECT * FROM playlist_songs WHERE playlist_id = :playlistId ORDER BY rowid")
    suspend fun findByPlaylistId(playlistId: String): List<PlaylistSongEntity>

    /** The playlists one song is already registered to. Filters on song_id alone, which is why
     * playlist_songs carries an index on that column. */
    @Query("SELECT playlist_id FROM playlist_songs WHERE song_id = :songId")
    suspend fun findPlaylistIdsBySongId(songId: String): List<String>

    /** A song already in the playlist is left where it is: the primary key rejects the duplicate
     * and IGNORE turns that into a no-op rather than a SQLiteConstraintException on a second tap.
     * Returns the new rowid, or -1 when the row was already there - with IGNORE there is no
     * exception to catch, so the return value is the only report that nothing was written. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: PlaylistSongEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId AND song_id IN (:songIds)")
    suspend fun deleteByPlaylistIdAndSongIdsChunk(playlistId: String, songIds: List<String>)

    /** See FavoriteSongDao.deleteBySongIds. playlistId is a host parameter of its own, so a chunk
     * that filled MAX_VARIABLES would put the statement one over. */
    @Transaction
    suspend fun deleteByPlaylistIdAndSongIds(playlistId: String, songIds: List<String>) {
        songIds.chunked(SqliteConstant.MAX_VARIABLES - 1).forEach {
            deleteByPlaylistIdAndSongIdsChunk(playlistId, it)
        }
    }

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)
}
