package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.tcode.thinmp.model.room.FavoriteArtistEntity

@Dao
interface FavoriteArtistDao {
    @Query("SELECT * FROM favorite_artists")
    suspend fun findAll(): List<FavoriteArtistEntity>

    @Query("SELECT * FROM favorite_artists WHERE artistId = :artistId")
    suspend fun findByArtistId(artistId: String): List<FavoriteArtistEntity>

    @Query("SELECT COUNT(*) > 0 FROM favorite_artists WHERE artistId = :artistId")
    suspend fun exists(artistId: String): Boolean

    @Insert
    suspend fun insert(entity: FavoriteArtistEntity)

    @Insert
    suspend fun insertAll(entities: List<FavoriteArtistEntity>)

    @Query("DELETE FROM favorite_artists")
    suspend fun deleteAll()

    @Query("DELETE FROM favorite_artists WHERE artistId = :artistId")
    suspend fun deleteByArtistId(artistId: String)

    @Query("DELETE FROM favorite_artists WHERE artistId IN (:artistIds)")
    suspend fun deleteByArtistIds(artistIds: List<String>)

    @Transaction
    suspend fun replaceAll(entities: List<FavoriteArtistEntity>) {
        deleteAll()
        insertAll(entities)
    }

    /** Reads the current state and writes as one unit; separately they race and two concurrent
     * toggles can both see "not a favourite" and insert a second row for the same artist. */
    @Transaction
    suspend fun toggle(artistId: String) {
        if (exists(artistId)) {
            deleteByArtistId(artistId)
        } else {
            insert(FavoriteArtistEntity(artistId = artistId))
        }
    }
}
