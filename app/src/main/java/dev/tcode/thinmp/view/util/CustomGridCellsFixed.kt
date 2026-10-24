package dev.tcode.thinmp.view.util

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant

/**
 * LazyVerticalGridの各行で、最初と最後の要素の幅を変えるGridCells。
 * GridCells.Fixedの代わりに使う。
 * 最初と最後を区別するため、2列以上でしか使えない。
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

    /**
     * LazyVerticalGridは列幅の計算結果をremember(columns, ...)で保持するので、
     * 列数と余白が同じなら等しいと判定しないと、recompositionのたびに計算し直される。
     */
    override fun equals(other: Any?): Boolean {
        return other is CustomGridCellsFixed && count == other.count && edgeSpace == other.edgeSpace
    }

    override fun hashCode(): Int {
        return 31 * count + edgeSpace.hashCode()
    }
}
