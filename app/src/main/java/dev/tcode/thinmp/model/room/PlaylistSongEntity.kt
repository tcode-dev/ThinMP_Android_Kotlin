package dev.tcode.thinmp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * `song_id` is deliberately left out of the index: no query filters on it alone, and the one query
 * that mentions it is already narrowed to a single playlist's rows by `playlist_id`. Adding it
 * would only widen the entries that `updatePlaylist` rewrites on every save.
 */
@Entity(
    tableName = "playlist_songs",
    indices = [Index(value = ["playlist_id"])]
)
data class PlaylistSongEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "playlist_id")
    val playlistId: String = "",
    @ColumnInfo(name = "song_id")
    val songId: String = ""
)
