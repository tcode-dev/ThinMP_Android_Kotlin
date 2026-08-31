package dev.tcode.thinmp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * A song is either in a playlist or it is not, so the pair is the identity of the row and there is
 * nothing for a surrogate key to add. Making it the primary key is what forbids the same song being
 * registered to one playlist twice, and it indexes `playlist_id`, which is the column the queries
 * that name a playlist filter on — a leading column of the primary key's index serves those on its
 * own.
 *
 * `song_id` carries an index of its own because `findPlaylistIdsBySongId` filters on it alone, and
 * the primary key's index cannot serve that — `song_id` is not its leading column.
 *
 * The order of a playlist is the order its rows were inserted in, which is why `findByPlaylistId`
 * asks for `rowid` explicitly rather than leaving it to whichever index SQLite picks.
 */
@Entity(tableName = "playlist_songs", primaryKeys = ["playlist_id", "song_id"], indices = [Index(value = ["song_id"])])
data class PlaylistSongEntity(
    @ColumnInfo(name = "playlist_id")
    val playlistId: String = "",
    @ColumnInfo(name = "song_id")
    val songId: String = ""
)
