package dev.tcode.thinmp.repository.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.tcode.thinmp.model.room.ShortcutEntity

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY `order` DESC")
    fun findAll(): List<ShortcutEntity>

    @Query("SELECT * FROM shortcuts WHERE itemId = :itemId AND type = :type")
    fun findByItemIdAndType(itemId: String, type: Int): List<ShortcutEntity>

    @Query("SELECT COUNT(*) > 0 FROM shortcuts WHERE itemId = :itemId AND type = :type")
    fun exists(itemId: String, type: Int): Boolean

    @Query("SELECT COALESCE(MAX(`order`), 0) FROM shortcuts")
    fun getMaxOrder(): Int

    @Insert
    fun insert(entity: ShortcutEntity)

    @Update
    fun update(entity: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE itemId = :itemId AND type = :type")
    fun deleteByItemIdAndType(itemId: String, type: Int)

    @Query("DELETE FROM shortcuts WHERE id IN (:ids)")
    fun deleteByIds(ids: List<String>)
}
