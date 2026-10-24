package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.tcode.thinmp.constant.SqliteConstant
import dev.tcode.thinmp.model.room.FavoriteArtistEntity

@Dao
interface FavoriteArtistDao {
    @Query("SELECT * FROM favorite_artists")
    suspend fun findAll(): List<FavoriteArtistEntity>

    @Query("SELECT * FROM favorite_artists WHERE artist_id = :artistId")
    suspend fun findByArtistId(artistId: String): List<FavoriteArtistEntity>

    @Query("SELECT COUNT(*) > 0 FROM favorite_artists WHERE artist_id = :artistId")
    suspend fun exists(artistId: String): Boolean

    @Insert
    suspend fun insert(entity: FavoriteArtistEntity)

    @Insert
    suspend fun insertAll(entities: List<FavoriteArtistEntity>)

    @Query("DELETE FROM favorite_artists")
    suspend fun deleteAll()

    @Query("DELETE FROM favorite_artists WHERE artist_id = :artistId")
    suspend fun deleteByArtistId(artistId: String)

    @Query("DELETE FROM favorite_artists WHERE artist_id IN (:artistIds)")
    suspend fun deleteByArtistIdsChunk(artistIds: List<String>)

    /** FavoriteSongDao.deleteBySongIdsを参照。 */
    @Transaction
    suspend fun deleteByArtistIds(artistIds: List<String>) {
        artistIds.chunked(SqliteConstant.MAX_VARIABLES).forEach { deleteByArtistIdsChunk(it) }
    }

    @Transaction
    suspend fun replaceAll(entities: List<FavoriteArtistEntity>) {
        deleteAll()
        insertAll(entities)
    }

    /**
     * 状態の確認と書き込みを1つのトランザクションで行う。
     * 別々にすると、同時に2回toggleしたときに両方が「未登録」と判断して2回挿入し、
     * 主キーの制約違反(SQLiteConstraintException)になる。
     */
    @Transaction
    suspend fun toggle(artistId: String) {
        if (exists(artistId)) {
            deleteByArtistId(artistId)
        } else {
            insert(FavoriteArtistEntity(artistId = artistId))
        }
    }
}
