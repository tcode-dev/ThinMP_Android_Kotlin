package dev.tcode.thinmp.repository.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.tcode.thinmp.model.room.PlaylistEntity

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY `order` ASC")
    fun findAll(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun findById(id: String): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE id IN (:ids)")
    fun findByIds(ids: List<String>): List<PlaylistEntity>

    @Query("SELECT COALESCE(MAX(`order`), 0) FROM playlists")
    fun getMaxOrder(): Int

    @Insert
    fun insert(entity: PlaylistEntity)

    @Update
    fun update(entity: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM playlists WHERE id IN (:ids)")
    fun deleteByIds(ids: List<String>)
}
