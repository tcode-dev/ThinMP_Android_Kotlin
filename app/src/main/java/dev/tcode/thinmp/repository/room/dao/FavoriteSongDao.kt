package dev.tcode.thinmp.repository.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.tcode.thinmp.model.room.FavoriteSongEntity

@Dao
interface FavoriteSongDao {
    @Query("SELECT * FROM favorite_songs")
    fun findAll(): List<FavoriteSongEntity>

    @Query("SELECT * FROM favorite_songs WHERE songId = :songId")
    fun findBySongId(songId: String): List<FavoriteSongEntity>

    @Query("SELECT COUNT(*) > 0 FROM favorite_songs WHERE songId = :songId")
    fun exists(songId: String): Boolean

    @Insert
    fun insert(entity: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs")
    fun deleteAll()

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    fun deleteBySongId(songId: String)

    @Query("DELETE FROM favorite_songs WHERE songId IN (:songIds)")
    fun deleteBySongIds(songIds: List<String>)
}
