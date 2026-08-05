package dev.tcode.thinmp.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import dev.tcode.thinmp.model.media.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class MediaStoreRepository<T : Music>(private val context: Context, private val uri: Uri, private val projection: Array<String>) {
    protected var cursor: Cursor? = null
    var selection: String? = null
    var selectionArgs: Array<String>? = null
    var sortOrder: String? = null

    private fun initialize() {
        cursor = createCursor()
    }

    abstract fun fetch(): T

    protected suspend fun get(): T? = withContext(Dispatchers.IO) {
        initialize()

        try {
            if (cursor?.moveToNext() != true) return@withContext null

            fetch()
        } finally {
            destroy()
        }
    }

    protected suspend fun getList(): List<T> = withContext(Dispatchers.IO) {
        initialize()

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

    private fun createCursor(): Cursor? {
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