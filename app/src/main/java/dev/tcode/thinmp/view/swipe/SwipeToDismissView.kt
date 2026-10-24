package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * 一覧はitemのkeyに行のidを使うので、このcompositionは行と一緒に破棄され、
 * dismissのstateが次の行に引き継がれることはない。
 * そのため、1つの一覧に同じidが2回現れてはいけない。
 * プレイリストは重複を禁止し、お気に入りとショートカットはidそのものをkeyにしている。
 *
 * rememberDismissStateはconfirmStateChangeを作成時の1回しか受け取らないので、
 * コールバックはrememberUpdatedStateで最新のものを読む。
 * 編集画面のコールバックは行のインデックスを取り込んでおり、上の行が消えるとインデックスが変わるので、
 * 作成時のラムダのままだと違う行を消してしまう。
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismissView(callback: () -> Unit, content: @Composable RowScope.() -> Unit) {
    val currentCallback by rememberUpdatedState(callback)
    val dismissState = rememberDismissState(confirmStateChange = {
        if (it == DismissValue.DismissedToStart) {
            currentCallback()
            true
        } else {
            false
        }
    })
    SwipeToDismiss(state = dismissState, directions = setOf(DismissDirection.EndToStart), background = {}, dismissContent = content)
}
