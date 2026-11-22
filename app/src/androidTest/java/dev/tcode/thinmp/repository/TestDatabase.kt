package dev.tcode.thinmp.repository

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider

/**
 * あえてallowMainThreadQueries()なしで作る。
 * これを使うテストは、メインスレッドをブロックするDAO関数がないことの確認も兼ねる。
 *
 * [queryCallback]は、クエリを実行したスレッドでそのSQLを受け取る。
 * 実行した回数を数えたいテストのために、別のExecutorに回さず、呼び出しが戻る前に届くようにしている。
 */
fun createTestDatabase(queryCallback: ((String) -> Unit)? = null): AppDatabase {
    val builder = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(), AppDatabase::class.java
    )

    if (queryCallback != null) {
        builder.setQueryCallback(RoomDatabase.QueryCallback { sql, _ -> queryCallback(sql) }) { it.run() }
    }

    return builder.build()
}
