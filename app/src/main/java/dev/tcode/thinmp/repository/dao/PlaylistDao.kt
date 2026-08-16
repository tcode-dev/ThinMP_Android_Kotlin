package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tcode.thinmp.constant.SqliteConstant
import dev.tcode.thinmp.model.room.PlaylistEntity

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY `order` ASC")
    suspend fun findAll(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun findById(id: String): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE id IN (:ids)")
    suspend fun findByIdsChunk(ids: List<String>): List<PlaylistEntity>

    /** See FavoriteSongDao.deleteBySongIds. Reading in chunks is transactional too, so the caller
     * gets one snapshot of the table rather than a list assembled across concurrent writes. */
    @Transaction
    suspend fun findByIds(ids: List<String>): List<PlaylistEntity> {
        return ids.chunked(SqliteConstant.MAX_VARIABLES).flatMap { findByIdsChunk(it) }
    }

    @Query("SELECT COALESCE(MAX(`order`), 0) FROM playlists")
    suspend fun getMaxOrder(): Int

    @Insert
    suspend fun insert(entity: PlaylistEntity)

    @Update
    suspend fun update(entity: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM playlists WHERE id IN (:ids)")
    suspend fun deleteByIdsChunk(ids: List<String>)

    /** See FavoriteSongDao.deleteBySongIds. */
    @Transaction
    suspend fun deleteByIds(ids: List<String>) {
        ids.chunked(SqliteConstant.MAX_VARIABLES).forEach { deleteByIdsChunk(it) }
    }
}
