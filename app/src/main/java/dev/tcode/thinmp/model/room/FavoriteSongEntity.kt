package dev.tcode.thinmp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 1曲につきお気に入りは1行なので、MediaStoreのidをそのまま主キーにする。
 * サロゲートキーは要らない。
 * 主キーにすることで重複行が防がれ、クエリが絞り込む列にインデックスも付く。
 */
@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: String = ""
)
