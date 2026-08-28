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

/**
 * The query and the cursor it opens are arguments and locals, never fields. As fields they were
 * shared by every caller of the same instance: two coroutines querying one repository would
 * overwrite each other's selection between the assignment and the query, and close each other's
 * cursor mid-iteration. Nothing here is scoped to the instance, so the instance holds nothing.
 */
abstract class MediaStoreRepository<T : Music>(private val context: Context, private val uri: Uri, private val projection: Array<String>) {
    abstract fun fetch(cursor: Cursor): T

    protected suspend fun get(selection: String? = null, selectionArgs: Array<String>? = null): T? = withContext(Dispatchers.IO) {
        createCursor(selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToNext()) fetch(cursor) else null
        }
    }

    protected suspend fun getList(selection: String? = null, selectionArgs: Array<String>? = null, sortOrder: String? = null): List<T> =
        withContext(Dispatchers.IO) {
            toList(createCursor(selection, selectionArgs, sortOrder))
        }

    /**
     * [bundle] is for queries the selection/sortOrder arguments cannot express - a SQL GROUP BY, or
     * a LIMIT. It is an overload rather than a fourth argument beside them because it carries its
     * own selection and sort order: ContentResolver.query() takes either the three arguments or the
     * bundle, never both.
     */
    protected suspend fun getList(bundle: Bundle): List<T> = withContext(Dispatchers.IO) {
        toList(context.contentResolver.query(uri, projection, bundle, null))
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
            getList("$idColumn IN (${makePlaceholders(chunk.size)})$suffix", toStringArray(chunk))
        }
    }

    private fun toList(cursor: Cursor?): List<T> {
        val list: MutableList<T> = ArrayList()

        cursor?.use {
            while (it.moveToNext()) {
                list.add(fetch(it))
            }
        }

        return list
    }

    private fun toStringArray(list: List<String>): Array<String> {
        return list.toTypedArray()
    }

    private fun makePlaceholders(size: Int): String {
        return TextUtils.join(",", IntArray(size).map { "?" })
    }

    private fun createCursor(selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }
}
