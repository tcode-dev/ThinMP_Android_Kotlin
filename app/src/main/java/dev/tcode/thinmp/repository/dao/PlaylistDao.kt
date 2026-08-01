package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.tcode.thinmp.model.room.PlaylistEntity

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY `order` ASC")
    suspend fun findAll(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun findById(id: String): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<String>): List<PlaylistEntity>

    @Query("SELECT COALESCE(MAX(`order`), 0) FROM playlists")
    suspend fun getMaxOrder(): Int

    @Insert
    suspend fun insert(entity: PlaylistEntity)

    @Update
    suspend fun update(entity: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM playlists WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
