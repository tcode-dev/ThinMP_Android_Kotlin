package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val itemId: String = "",
    val type: Int = 0,
    val order: Int = 0
)
