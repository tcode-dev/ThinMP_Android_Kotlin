package dev.tcode.thinmp.repository

import android.net.Uri

/**
 * ContentResolver.query()がCursorではなくnullを返したこと。
 * 0件ならnullではなく0件のCursorが返るので、nullはプロバイダーが見つからないか失敗したことを表す。
 * 空の一覧にすると0件と区別できず、Roomのidを突き合わせるサービスがすべての登録を消してしまう。
 */
class MediaStoreQueryException(uri: Uri) : RuntimeException("ContentResolver.query() returned null: $uri")
