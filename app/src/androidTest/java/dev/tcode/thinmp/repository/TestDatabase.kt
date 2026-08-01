package dev.tcode.thinmp.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

/**
 * Deliberately built without allowMainThreadQueries(): these tests are also the check that no
 * DAO function has quietly gone back to being blocking.
 */
fun createTestDatabase(): AppDatabase {
    return Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(), AppDatabase::class.java
    ).build()
}
