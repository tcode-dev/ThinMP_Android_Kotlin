package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.tcode.thinmp.constant.SqliteConstant
import dev.tcode.thinmp.model.room.PlaylistSongEntity

@Dao
interface PlaylistSongDao {
    @Query("SELECT * FROM playlist_songs WHERE playlist_id = :playlistId")
    suspend fun findByPlaylistId(playlistId: String): List<PlaylistSongEntity>

    @Insert
    suspend fun insert(entity: PlaylistSongEntity)

    @Insert
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
