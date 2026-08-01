package dev.tcode.thinmp.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.tcode.thinmp.model.room.ShortcutEntity

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY `order` DESC")
    suspend fun findAll(): List<ShortcutEntity>

    @Query("SELECT * FROM shortcuts WHERE itemId = :itemId AND type = :type")
    suspend fun findByItemIdAndType(itemId: String, type: Int): List<ShortcutEntity>

    @Query("SELECT COUNT(*) > 0 FROM shortcuts WHERE itemId = :itemId AND type = :type")
    suspend fun exists(itemId: String, type: Int): Boolean

    @Query("SELECT COALESCE(MAX(`order`), 0) FROM shortcuts")
    suspend fun getMaxOrder(): Int

    @Insert
    suspend fun insert(entity: ShortcutEntity)

    @Update
    suspend fun update(entity: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE itemId = :itemId AND type = :type")
    suspend fun deleteByItemIdAndType(itemId: String, type: Int)

    @Query("DELETE FROM shortcuts WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    /** Reads the current maximum order and inserts as one unit; separately they race and two
     * concurrent adds can be assigned the same order. */
    @Transaction
    suspend fun insertAtEnd(itemId: String, type: Int) {
        insert(ShortcutEntity(itemId = itemId, type = type, order = getMaxOrder() + 1))
    }
}
