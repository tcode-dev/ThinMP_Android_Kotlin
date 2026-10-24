package dev.tcode.thinmp.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MediaStoreのidを主キーにする理由はFavoriteSongEntityを参照。
 */
@Entity(tableName = "favorite_artists")
data class FavoriteArtistEntity(
    @PrimaryKey
    @ColumnInfo(name = "artist_id")
    val artistId: String = ""
)
