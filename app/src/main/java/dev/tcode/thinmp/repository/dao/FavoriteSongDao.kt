package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.tcode.thinmp.model.room.FavoriteSongEntity

@Dao
interface FavoriteSongDao {
    @Query("SELECT * FROM favorite_songs")
    suspend fun findAll(): List<FavoriteSongEntity>

    @Query("SELECT * FROM favorite_songs WHERE songId = :songId")
    suspend fun findBySongId(songId: String): List<FavoriteSongEntity>

    @Query("SELECT COUNT(*) > 0 FROM favorite_songs WHERE songId = :songId")
    suspend fun exists(songId: String): Boolean

    @Insert
    suspend fun insert(entity: FavoriteSongEntity)

    @Insert
    suspend fun insertAll(entities: List<FavoriteSongEntity>)

    @Query("DELETE FROM favorite_songs")
    suspend fun deleteAll()

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteBySongId(songId: String)

    @Query("DELETE FROM favorite_songs WHERE songId IN (:songIds)")
    suspend fun deleteBySongIds(songIds: List<String>)

    @Transaction
    suspend fun replaceAll(entities: List<FavoriteSongEntity>) {
        deleteAll()
        insertAll(entities)
    }

    /** Reads the current state and writes as one unit; separately they race and two concurrent
     * toggles can both see "not a favourite" and insert a second row for the same song. */
    @Transaction
    suspend fun toggle(songId: String) {
        if (exists(songId)) {
            deleteBySongId(songId)
        } else {
            insert(FavoriteSongEntity(songId = songId))
        }
    }
}
