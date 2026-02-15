package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.tcode.thinmp.model.room.PlaylistSongEntity

@Dao
interface PlaylistSongDao {
    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    fun findByPlaylistId(playlistId: String): List<PlaylistSongEntity>

    @Insert
    fun insert(entity: PlaylistSongEntity)

    @Insert
    fun insertAll(entities: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId IN (:songIds)")
    fun deleteByPlaylistIdAndSongIds(playlistId: String, songIds: List<String>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    fun deleteByPlaylistId(playlistId: String)
}
