package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tcode.thinmp.constant.SqliteConstant
import dev.tcode.thinmp.model.room.ShortcutEntity

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY `order` DESC")
    suspend fun findAll(): List<ShortcutEntity>

    @Query("SELECT * FROM shortcuts WHERE item_id = :itemId AND type = :type")
    suspend fun findByItemIdAndType(itemId: String, type: Int): List<ShortcutEntity>

    @Query("SELECT COUNT(*) > 0 FROM shortcuts WHERE item_id = :itemId AND type = :type")
    suspend fun exists(itemId: String, type: Int): Boolean

    @Query("SELECT COALESCE(MAX(`order`), 0) FROM shortcuts")
    suspend fun getMaxOrder(): Int

    @Insert
    suspend fun insert(entity: ShortcutEntity)

    @Update
    suspend fun update(entity: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE item_id = :itemId AND type = :type")
    suspend fun deleteByItemIdAndType(itemId: String, type: Int)

    @Query("DELETE FROM shortcuts WHERE id IN (:ids)")
    suspend fun deleteByIdsChunk(ids: List<String>)

    /**
     * FavoriteSongDao.deleteBySongIdsを参照。
     */
    @Transaction
    suspend fun deleteByIds(ids: List<String>) {
        ids.chunked(SqliteConstant.MAX_VARIABLES).forEach { deleteByIdsChunk(it) }
    }

    /**
     * orderの最大値の取得と挿入を1つのトランザクションで行う。
     * 別々にすると、同時に追加したときに同じorderが付いてしまう。
     */
    @Transaction
    suspend fun insertAtEnd(itemId: String, type: Int) {
        insert(ShortcutEntity(itemId = itemId, type = type, order = getMaxOrder() + 1))
    }

    /**
     * 状態の確認と書き込みを1つのトランザクションで行う。
     * 別々にすると、同時に2回toggleしたときに両方が「未登録」と判断して、同じ項目が2行挿入される。
     */
    @Transaction
    suspend fun toggle(itemId: String, type: Int) {
        if (exists(itemId, type)) {
            deleteByItemIdAndType(itemId, type)
        } else {
            insert(ShortcutEntity(itemId = itemId, type = type, order = getMaxOrder() + 1))
        }
    }
}
