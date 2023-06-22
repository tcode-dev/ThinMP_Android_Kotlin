package dev.tcode.thinmp.view.cell

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant

const val first = 1
const val last = 0

@Composable
fun GridCellView(
    index: Int,
    colNumber: Int,
    content: @Composable BoxScope.() -> Unit,
) {
    // 2列の場合は端と要素間の余白を均等にする
    // 3列以上は余白を固定にする
    val position = ((index + 1) % colNumber)
    val start = if (colNumber == 2 && position == last) StyleConstant.PADDING_SMALL.dp else StyleConstant.PADDING_LARGE.dp
    val end = if (colNumber == 2 && position == first) StyleConstant.PADDING_SMALL.dp else StyleConstant.PADDING_LARGE.dp

    Box(modifier = Modifier.padding(start = start, end = end, bottom = StyleConstant.PADDING_LARGE.dp), content = content)
}