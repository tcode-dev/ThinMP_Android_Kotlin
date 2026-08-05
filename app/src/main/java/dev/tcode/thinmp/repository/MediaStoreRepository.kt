package dev.tcode.thinmp.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
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

    protected fun toStringArray(list: List<String>): Array<String> {
        return list.toTypedArray()
    }

    protected fun makePlaceholders(size: Int): String {
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