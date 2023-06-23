package dev.tcode.thinmp.view.util

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant
import okhttp3.internal.toImmutableList

/**
 * GridCellsを拡張してLazyVerticalGridの各行の最初と最後の要素の幅を変更する
 * GridCells.Fixedの代わりに使う
 */
class CustomGridCellsFixed(
    private val count: Int,
    private val edgeSpace: Dp = StyleConstant.PADDING_SMALL.dp
) : GridCells {

    init {
        require(count > 0)
    }

    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val contentSize = availableSize - (edgeSpace.roundToPx() * 2)
        val baseSize = contentSize / count
        val edgeSize = baseSize + edgeSpace.roundToPx()
        val baseCount = count - 2
        val list = MutableList(baseCount) { baseSize }

        list.add(0, edgeSize)
        list.add(edgeSize)

        return list.toImmutableList()
    }
}