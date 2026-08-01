package dev.tcode.thinmp.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A song is either a favourite or it is not, so the MediaStore id is the identity of the row and
 * there is nothing for a surrogate key to add. Making it the primary key is also what forbids the
 * duplicate rows that `toggle` exists to avoid, and it indexes the column every query filters on.
 */
@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    val songId: String = ""
)
