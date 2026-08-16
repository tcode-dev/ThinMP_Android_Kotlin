package dev.tcode.thinmp.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import dev.tcode.thinmp.constant.SqliteConstant
import dev.tcode.thinmp.model.media.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class MediaStoreRepository<T : Music>(private val context: Context, private val uri: Uri, private val projection: Array<String>) {
    protected var cursor: Cursor? = null
    var selection: String? = null
    var selectionArgs: Array<String>? = null
    var sortOrder: String? = null

    private fun initialize(bundle: Bundle?) {
        cursor = createCursor(bundle)
    }

    abstract fun fetch(): T

    protected suspend fun get(): T? = withContext(Dispatchers.IO) {
        initialize(null)

        try {
            if (cursor?.moveToNext() != true) return@withContext null

            fetch()
        } finally {
            destroy()
        }
    }

    /**
     * [bundle] is for queries the selection/sortOrder fields cannot express - a SQL GROUP BY, or a
     * LIMIT. It is a parameter rather than another field beside them because it replaces all three:
     * a field would have to be cleared by every other query on the way past, and forgetting once
     * would silently apply the previous call's grouping.
     */
    protected suspend fun getList(bundle: Bundle? = null): List<T> = withContext(Dispatchers.IO) {
        initialize(bundle)

        try {
            val list: MutableList<T> = ArrayList()

            while (cursor?.moveToNext() == true) {
                list.add(fetch())
            }

            list
        } finally {
            destroy()
        }
    }

    /**
     * An `IN` list costs one host parameter per id, and these statements are compiled by
     * MediaProvider - a separate process, with a SQLite build of its own and a limit this side
     * cannot read - so the list is cut at [SqliteConstant.MAX_VARIABLES] rather than sent whole.
     *
     * Split, it becomes several queries and several cursors, and the rows arrive grouped by chunk
     * instead of in one provider order. That is already how callers treat a findByIds(): each maps
     * its own id list over the result rather than using the order it came back in, because
     * MediaStore has no way to sort by it.
     *
     * [ids] is deduplicated first. `IN` collapses repeats anyway, and the same song listed a
     * thousand times in one playlist should not be what pushes the query over the limit.
     */
    protected suspend fun getListByIds(idColumn: String, ids: List<String>, condition: String? = null): List<T> {
        val suffix = if (condition == null) "" else " AND $condition"

        return ids.distinct().chunked(SqliteConstant.MAX_VARIABLES).flatMap { chunk ->
            selection = "$idColumn IN (${makePlaceholders(chunk.size)})$suffix"
            selectionArgs = toStringArray(chunk)
            sortOrder = null

            getList()
        }
    }

    private fun toStringArray(list: List<String>): Array<String> {
        return list.toTypedArray()
    }

    private fun makePlaceholders(size: Int): String {
        return TextUtils.join(",", IntArray(size).map { "?" })
    }

    private fun createCursor(bundle: Bundle?): Cursor? {
        if (bundle != null) {
            return context.contentResolver.query(uri, projection, bundle, null)
        }

        return context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }

    private fun destroy() {
        cursor?.close()
        cursor = null
    }
}