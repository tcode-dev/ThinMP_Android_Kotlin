package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/** See FavoriteSongEntity for why the MediaStore id is the primary key. */
@Entity(tableName = "favorite_artists")
data class FavoriteArtistEntity(
    @PrimaryKey
    val artistId: String = ""
)
