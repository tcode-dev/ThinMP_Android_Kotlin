package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val playlistId: String = "",
    val songId: String = ""
)
