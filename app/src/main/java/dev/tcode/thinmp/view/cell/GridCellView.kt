package dev.tcode.thinmp.view.cell

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GridCellView(
    index: Int,
    colNumber: Int,
    itemSize: Dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val even = (index % colNumber) == 0
    val start = if (even) 20.dp else 10.dp
    val end = if (even) 10.dp else 20.dp
    Box(
        modifier = Modifier
            .width(itemSize)
            .padding(
                start = start,
                end = end,
                top = 0.dp,
                bottom = 20.dp
            ),
        content = content
    )
}
