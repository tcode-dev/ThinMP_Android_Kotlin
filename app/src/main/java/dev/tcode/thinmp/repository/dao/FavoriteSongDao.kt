package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.tcode.thinmp.constant.SqliteConstant
import dev.tcode.thinmp.model.room.FavoriteSongEntity

@Dao
interface FavoriteSongDao {
    @Query("SELECT * FROM favorite_songs")
    suspend fun findAll(): List<FavoriteSongEntity>

    @Query("SELECT * FROM favorite_songs WHERE song_id = :songId")
    suspend fun findBySongId(songId: String): List<FavoriteSongEntity>

    @Query("SELECT COUNT(*) > 0 FROM favorite_songs WHERE song_id = :songId")
    suspend fun exists(songId: String): Boolean

    @Insert
    suspend fun insert(entity: FavoriteSongEntity)

    @Insert
    suspend fun insertAll(entities: List<FavoriteSongEntity>)

    @Query("DELETE FROM favorite_songs")
    suspend fun deleteAll()

    @Query("DELETE FROM favorite_songs WHERE song_id = :songId")
    suspend fun deleteBySongId(songId: String)

    @Query("DELETE FROM favorite_songs WHERE song_id IN (:songIds)")
    suspend fun deleteBySongIdsChunk(songIds: List<String>)

    /**
     * [SqliteConstant.MAX_VARIABLES]を超えないよう分割して削除する。
     * トランザクションにするので、すべて消えるか、1件も消えないかのどちらかになる。
     */
    @Transaction
    suspend fun deleteBySongIds(songIds: List<String>) {
        songIds.chunked(SqliteConstant.MAX_VARIABLES).forEach { deleteBySongIdsChunk(it) }
    }

    @Transaction
    suspend fun replaceAll(entities: List<FavoriteSongEntity>) {
        deleteAll()
        insertAll(entities)
    }

    /**
     * 状態の確認と書き込みを1つのトランザクションで行う。
     * 別々にすると、同時に2回toggleしたときに両方が「未登録」と判断して2回挿入し、
     * 主キーの制約違反(SQLiteConstraintException)になる。
     */
    @Transaction
    suspend fun toggle(songId: String) {
        if (exists(songId)) {
            deleteBySongId(songId)
        } else {
            insert(FavoriteSongEntity(songId = songId))
        }
    }
}
