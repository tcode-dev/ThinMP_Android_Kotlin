package dev.tcode.thinmp.view.util

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * LazyVerticalGridは行の幅を、またがるセルの幅の合計で決める。
 * ここで返す合計がavailableSizeに足りないと、
 * セルだけでなく全幅のアイテム(メニューの行や曲の一覧)まで幅が足りなくなる。
 */
class CustomGridCellsFixedTest {
    private val edge = 10

    /** 端のセルはGridCellViewが内側に10dp多く余白を取るので、そのぶん10dp広くする。 */
    @Test
    fun widensTheFirstAndLastCellByTheEdgeSpace() {
        assertEquals(listOf(510, 510), calculate(count = 2, availableSize = 1020))
        assertEquals(listOf(340, 330, 340), calculate(count = 3, availableSize = 1010))
        assertEquals(listOf(260, 250, 250, 260), calculate(count = 4, availableSize = 1020))
    }

    /** 端と内側の幅の差がedgeSpaceと等しくないと、見える内容の幅が列によって変わってしまう。 */
    @Test
    fun keepsTheVisibleWidthOfEveryCellEqual() {
        val sizes = calculate(count = 4, availableSize = 1020)

        assertEquals(edge, sizes.first() - sizes[1])
        assertEquals(edge, sizes.last() - sizes[2])
    }

    @Test
    fun fillsTheAvailableSize() {
        assertEquals(1020, calculate(count = 4, availableSize = 1020).sum())
    }

    /** 割り切れない端数を捨てると、行の幅が最大でcount-1px足りなくなる。 */
    @Test
    fun distributesTheRemainder() {
        // 411dp・density 2.625の端末。contentSizeが1027なので1px余る。
        val sizes = calculate(count = 2, availableSize = 1079, density = 2.625f)

        assertEquals(listOf(540, 539), sizes)
        assertEquals(1079, sizes.sum())
    }

    /** GridCellsの仕様では、合計はセル間の余白を除いた幅でなければならない。 */
    @Test
    fun subtractsTheSpacingBetweenCells() {
        val spacing = 10
        val sizes = calculate(count = 3, availableSize = 1010, spacing = spacing)

        assertEquals(1010 - (spacing * 2), sizes.sum())
    }

    /** 2列未満では最初と最後を区別できないので、幅を計算できない。 */
    @Test
    fun rejectsAColumnCountBelowTwo() {
        assertThrows(IllegalArgumentException::class.java) { CustomGridCellsFixed(1) }
        assertThrows(IllegalArgumentException::class.java) { CustomGridCellsFixed(0) }
        assertThrows(IllegalArgumentException::class.java) { CustomGridCellsFixed(-1) }
    }

    /**
     * 画面はrecompositionのたびにインスタンスを作り直すので、
     * 等しいと判定しないとremember(columns, ...)に保持した列幅が毎回計算し直される。
     */
    @Test
    fun treatsTheSameColumnCountAndEdgeSpaceAsEqual() {
        assertEquals(CustomGridCellsFixed(2, edge.dp), CustomGridCellsFixed(2, edge.dp))
        assertEquals(CustomGridCellsFixed(2, edge.dp).hashCode(), CustomGridCellsFixed(2, edge.dp).hashCode())
        assertEquals(CustomGridCellsFixed(3), CustomGridCellsFixed(3))
    }

    /** 列数か余白が違えば幅も変わるので、別のものとして扱う。 */
    @Test
    fun treatsDifferentColumnCountOrEdgeSpaceAsNotEqual() {
        assertNotEquals(CustomGridCellsFixed(2, edge.dp), CustomGridCellsFixed(3, edge.dp))
        assertNotEquals(CustomGridCellsFixed(2, edge.dp), CustomGridCellsFixed(2, (edge + 1).dp))
        assertNotEquals(CustomGridCellsFixed(2, edge.dp), GridCells.Fixed(2))
    }

    private fun calculate(count: Int, availableSize: Int, spacing: Int = 0, density: Float = 1f): List<Int> {
        val cells = CustomGridCellsFixed(count, edge.dp)

        return with(cells) { with(Density(density)) { calculateCrossAxisCellSizes(availableSize, spacing) } }
    }
}
