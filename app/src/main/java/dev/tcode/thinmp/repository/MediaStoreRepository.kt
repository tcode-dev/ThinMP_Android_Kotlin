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
 * selectionやカーソルはフィールドに持たず、引数とローカル変数だけで扱う。
 * フィールドにすると、同じインスタンスを使う複数のコルーチンが互いのselectionを上書きしたり、
 * 読み込み中のカーソルを閉じたりしてしまう。
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
     * selection/sortOrderでは書けないクエリ(GROUP BYやLIMIT)用。
     * ContentResolver.query()はselectionなどの引数かbundleのどちらか一方しか受け取らないので、
     * 引数を足すのではなくオーバーロードにしている。
     */
    protected suspend fun getList(bundle: Bundle): List<T> = withContext(Dispatchers.IO) {
        toList(context.contentResolver.query(uri, projection, bundle, null))
    }

    /**
     * `IN`はidの数だけホストパラメーターを使い、
     * その上限は別プロセスのMediaProviderのSQLite次第でアプリからは分からない。
     * そのため[SqliteConstant.MAX_VARIABLES]ごとに分割して問い合わせる。
     *
     * 分割すると結果はチャンクごとにまとまって返るが、MediaStoreではidの順に並べられないので、
     * 呼び出し側はもともと戻り値の順序を使わず、自分のidリストの順に対応付けている。
     *
     * [ids]は先に重複を除く。
     * `IN`は重複をまとめるので結果は変わらず、
     * 同じ曲が大量に入ったプレイリストで上限を超えることもなくなる。
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
