package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * 一覧はitemのkeyに行のidを使うので、このcompositionは行と一緒に破棄され、
 * dismissのstateが次の行に引き継がれることはない。
 * そのため、1つの一覧に同じidが2回現れてはいけない。
 * プレイリストは重複を禁止し、お気に入りとショートカットはidそのものをkeyにしている。
 *
 * confirmValueChangeは指を離したときとアニメーションが止まったときの両方で呼ばれ、1回のスワイプで2回呼ばれる。
 * そのため、コールバックはconfirmValueChangeではなく、currentValueがEndToStartに変わったときに1回だけ呼ぶ。
 *
 * 編集画面のコールバックは行のインデックスを取り込んでおり、上の行が消えるとインデックスが変わる。
 * effectはcurrentValueが変わったcompositionで始まり、そのcompositionのcallbackを呼ぶので、
 * 行を作ったときの古いインデックスで違う行を消すことはない。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissView(callback: () -> Unit, content: @Composable RowScope.() -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState()
    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
        LaunchedEffect(Unit) { callback() }
    }
    SwipeToDismissBox(state = dismissState, backgroundContent = {}, enableDismissFromStartToEnd = false, content = content)
}
