package dev.tcode.thinmp.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

/**
 * あえてallowMainThreadQueries()なしで作る。
 * これを使うテストは、メインスレッドをブロックするDAO関数がないことの確認も兼ねる。
 */
fun createTestDatabase(): AppDatabase {
    return Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(), AppDatabase::class.java
    ).build()
}
