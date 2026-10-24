package dev.tcode.thinmp.constant

/**
 * `IN (:ids)`はidの数だけホストパラメーターを使い、
 * SQLiteはSQLITE_MAX_VARIABLE_NUMBERを超える文を拒否する。
 * つまりクエリが通るかどうかはユーザーのデータ(お気に入り、プレイリスト、ショートカット)の件数で
 * 決まるので、idのリストはこの数ごとに分割して送る。
 *
 * 上限は3.32より前のSQLiteでは999、それ以降は32766。
 * MediaStoreのクエリは別プロセスのMediaProviderが自前のSQLiteで、
 * Roomのクエリは端末のSQLiteで実行されるので、どちらでも安全な999にしている。
 *
 * リスト以外にもパラメーターを使うクエリは、その分を差し引く(PlaylistSongDaoを参照)。
 */
class SqliteConstant {
    companion object {
        const val MAX_VARIABLES = 999
    }
}
