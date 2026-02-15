package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.tcode.thinmp.model.room.FavoriteArtistEntity

@Dao
interface FavoriteArtistDao {
    @Query("SELECT * FROM favorite_artists")
    fun findAll(): List<FavoriteArtistEntity>

    @Query("SELECT * FROM favorite_artists WHERE artistId = :artistId")
    fun findByArtistId(artistId: String): List<FavoriteArtistEntity>

    @Query("SELECT COUNT(*) > 0 FROM favorite_artists WHERE artistId = :artistId")
    fun exists(artistId: String): Boolean

    @Insert
    fun insert(entity: FavoriteArtistEntity)

    @Query("DELETE FROM favorite_artists")
    fun deleteAll()

    @Query("DELETE FROM favorite_artists WHERE artistId = :artistId")
    fun deleteByArtistId(artistId: String)

    @Query("DELETE FROM favorite_artists WHERE artistId IN (:artistIds)")
    fun deleteByArtistIds(artistIds: List<String>)
}
