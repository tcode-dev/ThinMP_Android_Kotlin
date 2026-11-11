package dev.tcode.thinmp.viewModel

import dev.tcode.thinmp.repository.MediaStoreQueryException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaStoreの問い合わせが失敗したかどうか。
 * アプリ全体で1つだけ持ち、ErrorViewがこれを見て画面をエラー表示に替える。
 * どの画面の読み込みで失敗しても、再読み込みはメイン画面からやり直すので、画面ごとには持たない。
 */
@Singleton
class MediaStoreFailure @Inject constructor() {
    private val _failed = MutableStateFlow(false)
    val failed: StateFlow<Boolean> = _failed.asStateFlow()

    /**
     * [block]がMediaStoreQueryExceptionで止まったら、失敗の状態にする。
     * ほかの例外はプログラムの誤りか回復できないものなので、捕まえない。
     * 捕まえる例外の型を1か所で決めるために関数にしている。
     * inlineなので、[block]の中でsuspend関数を呼んだり`return@launch`したりできる。
     */
    inline fun catching(block: () -> Unit) {
        try {
            block()
        } catch (e: MediaStoreQueryException) {
            report()
        }
    }

    fun report() {
        _failed.value = true
    }

    fun reset() {
        _failed.value = false
    }
}
