package dev.tcode.thinmp.view.util

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant

/**
 * GridCellsを拡張してLazyVerticalGridの各行の最初と最後の要素の幅を変更する
 * GridCells.Fixedの代わりに使う
 * 最初と最後を区別するため2列以上でのみ使える
 */
class CustomGridCellsFixed(
    private val count: Int,
    private val edgeSpace: Dp = StyleConstant.PADDING_SMALL.dp
) : GridCells {

    init {
        require(count > 1)
    }

    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val edge = edgeSpace.roundToPx()
        val contentSize = availableSize - (spacing * (count - 1)) - (edge * 2)
        val baseSize = contentSize / count
        val remainder = contentSize % count

        return List(count) { index ->
            val extra = if (index == 0 || index == count - 1) edge else 0

            baseSize + (if (index < remainder) 1 else 0) + extra
        }
    }
}
