package dev.tcode.thinmp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * 同じ曲は1つのプレイリストに1回だけなので、(playlist_id, song_id)をそのまま主キーにする。
 * これで重複登録が防がれ、主キーのインデックスの先頭がplaylist_idなので、
 * プレイリストで絞り込むクエリにも使える。
 *
 * `findPlaylistIdsBySongId`は`song_id`だけで絞り込むが、主キーのインデックスは使えないので、
 * `song_id`に別のインデックスを付ける。
 *
 * プレイリストの曲順は挿入順。
 * `findByPlaylistId`はインデックスの順序に頼らず`rowid`で並べる。
 */
@Entity(tableName = "playlist_songs", primaryKeys = ["playlist_id", "song_id"], indices = [Index(value = ["song_id"])])
data class PlaylistSongEntity(
    @ColumnInfo(name = "playlist_id")
    val playlistId: String = "",
    @ColumnInfo(name = "song_id")
    val songId: String = ""
)
