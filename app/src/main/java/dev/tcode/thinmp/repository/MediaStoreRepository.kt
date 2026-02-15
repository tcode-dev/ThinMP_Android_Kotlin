package dev.tcode.thinmp.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import dev.tcode.thinmp.model.media.Music

abstract class MediaStoreRepository<T : Music>(private val context: Context, private val uri: Uri, private val projection: Array<String>) {
    protected var cursor: Cursor? = null
    var selection: String? = null
    var selectionArgs: Array<String>? = null
    var sortOrder: String? = null
    var bundle: Bundle? = null

    private fun initialize() {
        cursor = createCursor()
    }

    abstract fun fetch(): T

    protected fun get(): T? {
        initialize()

        if (!cursor?.moveToNext()!!) return null

        val item: T = fetch()

        destroy()

        return item
    }

    protected fun getList(): List<T> {
        initialize()

        val list: MutableList<T> = ArrayList()

        while (cursor?.moveToNext() == true) {
            list.add(fetch())
        }

        destroy()

        return list
    }

    protected fun toStringArray(list: List<String>): Array<String> {
        return list.toTypedArray()
    }

    protected fun makePlaceholders(size: Int): String {
        return TextUtils.join(",", IntArray(size).map { "?" })
    }

    private fun createCursor(): Cursor? {
        if (bundle != null) {
            return context.contentResolver.query(
                uri,
                projection,
                bundle,
                null
            )
        } else {
            return context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
        }
    }

    private fun destroy() {
        cursor?.close()
        cursor = null
    }
}