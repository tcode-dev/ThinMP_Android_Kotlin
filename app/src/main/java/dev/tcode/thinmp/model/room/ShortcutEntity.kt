package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/** Every shortcut lookup keys on the (itemId, type) pair, so one composite index covers them all. */
@Entity(
    tableName = "shortcuts",
    indices = [Index(value = ["itemId", "type"])]
)
data class ShortcutEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val itemId: String = "",
    val type: Int = 0,
    val order: Int = 0
)
