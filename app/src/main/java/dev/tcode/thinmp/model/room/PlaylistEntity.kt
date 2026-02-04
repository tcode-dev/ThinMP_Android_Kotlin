package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val order: Int = 0
)
