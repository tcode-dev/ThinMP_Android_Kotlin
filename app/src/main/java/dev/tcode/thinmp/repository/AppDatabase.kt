package dev.tcode.thinmp.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.tcode.thinmp.model.room.FavoriteArtistEntity
import dev.tcode.thinmp.model.room.FavoriteSongEntity
import dev.tcode.thinmp.model.room.PlaylistEntity
import dev.tcode.thinmp.model.room.PlaylistSongEntity
import dev.tcode.thinmp.model.room.ShortcutEntity
import dev.tcode.thinmp.repository.dao.FavoriteArtistDao
import dev.tcode.thinmp.repository.dao.FavoriteSongDao
import dev.tcode.thinmp.repository.dao.PlaylistDao
import dev.tcode.thinmp.repository.dao.PlaylistSongDao
import dev.tcode.thinmp.repository.dao.ShortcutDao

@Database(
    entities = [
        FavoriteSongEntity::class,
        FavoriteArtistEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        ShortcutEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun favoriteArtistDao(): FavoriteArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thinmp_database"
                )
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
