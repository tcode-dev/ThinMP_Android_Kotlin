package dev.tcode.thinmp.view.cell

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun GridCellView(
    index: Int,
    colNumber: Int,
    itemSize: Dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val even = (index % colNumber) == 0
    val start = if (even) StyleConstant.PADDING_LARGE.dp else StyleConstant.PADDING_SMALL.dp
    val end = if (even) StyleConstant.PADDING_SMALL.dp else StyleConstant.PADDING_LARGE.dp

    Box(
        modifier = Modifier
            .width(itemSize)
            .padding(
                start = start, end = end, bottom = StyleConstant.PADDING_LARGE.dp
            ), content = content
    )
}