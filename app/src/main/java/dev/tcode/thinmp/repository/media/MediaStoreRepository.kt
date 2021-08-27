package dev.tcode.thinmp.repository.media

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import dev.tcode.thinmp.model.Music

abstract class MediaStoreRepository<T : Music>(private val context: Context, private val uri: Uri, private val projection: Array<String>) {
    protected var cursor: Cursor? = null
//    lateinit var uri: Uri
//    lateinit var projection: Array<String>
    lateinit var selection: String
    var selectionArgs: Array<String>? = null
    var sortOrder: String? = null

    init {
        cursor = createCursor();
    }

    abstract fun fetch(): T

    protected fun get(): T? {
        if (!cursor?.moveToNext()!!) return null

        val item: T? = fetch()

        destroy()

        return item
    }

    protected fun getList(): List<T> {
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
        return TextUtils.join(",", listOf(0, size).map { i -> "?" })
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