package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "favorite_artists",
    indices = [Index(value = ["artistId"])]
)
data class FavoriteArtistEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val artistId: String = ""
)
