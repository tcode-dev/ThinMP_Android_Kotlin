package dev.tcode.thinmp.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = true
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

        /**
         * Adds the lookup indexes. Every column below is one an existing query already filters on,
         * and the index names are the ones Room derives from the entities' [androidx.room.Index]
         * annotations — they have to match exactly or Room's schema validation rejects the upgrade.
         * The indexes are deliberately not UNIQUE: installs that predate `FavoriteSongDao.toggle`
         * can hold duplicate rows, and a unique index would make this migration crash on them.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_favorite_songs_songId` ON `favorite_songs` (`songId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_favorite_artists_artistId` ON `favorite_artists` (`artistId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_songs_playlistId` ON `playlist_songs` (`playlistId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shortcuts_itemId_type` ON `shortcuts` (`itemId`, `type`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thinmp_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
