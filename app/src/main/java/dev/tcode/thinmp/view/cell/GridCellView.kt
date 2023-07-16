package dev.tcode.thinmp.view.cell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
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
    val position = ((index + 1) % colNumber)
    val start = if (position == first) StyleConstant.PADDING_LARGE.dp else StyleConstant.PADDING_SMALL.dp
    val end = if (position == last) StyleConstant.PADDING_LARGE.dp else StyleConstant.PADDING_SMALL.dp

    Box(modifier = Modifier.padding(start = start, end = end, bottom = StyleConstant.PADDING_LARGE.dp), content = content)
}