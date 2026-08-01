package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * `songId` is deliberately left out of the index: no query filters on it alone, and the one query
 * that mentions it is already narrowed to a single playlist's rows by `playlistId`. Adding it would
 * only widen the entries that `updatePlaylist` rewrites on every save.
 */
@Entity(
    tableName = "playlist_songs",
    indices = [Index(value = ["playlistId"])]
)
data class PlaylistSongEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val playlistId: String = "",
    val songId: String = ""
)
