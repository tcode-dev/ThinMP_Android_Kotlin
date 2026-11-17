package dev.tcode.thinmp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * ショートカットの検索はすべて(item_id, type)の組で行うので、複合インデックス1つで足りる。
 * 同じ項目のショートカットが2行あると一覧のkeyがぶつかって落ちるので、uniqueにして重複を禁止する。
 */
@Entity(
    tableName = "shortcuts",
    indices = [Index(value = ["item_id", "type"], unique = true)]
)
data class ShortcutEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "item_id")
    val itemId: String = "",
    val type: Int = 0,
    val order: Int = 0
)
