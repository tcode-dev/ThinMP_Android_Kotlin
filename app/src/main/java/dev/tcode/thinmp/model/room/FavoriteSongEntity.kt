package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val songId: String = ""
)
