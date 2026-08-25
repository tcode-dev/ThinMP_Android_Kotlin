package dev.tcode.thinmp.view.util

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * LazyVerticalGridは1行の幅を「またいだスロットの左端から右端まで」で決めるため、ここが返す合計が
 * availableSizeに届かないと、セルだけでなく全幅アイテム(メニュー行や曲一覧)の幅も足りなくなる。
 */
class CustomGridCellsFixedTest {
    private val edge = 10

    /** 端の要素はGridCellViewが内側に10dp多くパディングを取るぶん、幅も10dp広い。 */
    @Test
    fun widensTheFirstAndLastCellByTheEdgeSpace() {
        assertEquals(listOf(510, 510), calculate(count = 2, availableSize = 1020))
        assertEquals(listOf(340, 330, 340), calculate(count = 3, availableSize = 1010))
        assertEquals(listOf(260, 250, 250, 260), calculate(count = 4, availableSize = 1020))
    }

    /** 端と内側の差がedgeSpaceちょうどでなければ、見えるコンテンツの幅が列ごとに変わってしまう。 */
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
        // 411dp / density 2.625 の端末。contentSizeが1027なので1px余る
        val sizes = calculate(count = 2, availableSize = 1079, density = 2.625f)

        assertEquals(listOf(540, 539), sizes)
        assertEquals(1079, sizes.sum())
    }

    /** GridCellsの取り決めでは、返す合計はセルの間隔を除いた幅でなければならない。 */
    @Test
    fun subtractsTheSpacingBetweenCells() {
        val spacing = 10
        val sizes = calculate(count = 3, availableSize = 1010, spacing = spacing)

        assertEquals(1010 - (spacing * 2), sizes.sum())
    }

    /** 最初と最後を区別できない列数は、幅の計算そのものが成立しない。 */
    @Test
    fun rejectsAColumnCountBelowTwo() {
        assertThrows(IllegalArgumentException::class.java) { CustomGridCellsFixed(1) }
        assertThrows(IllegalArgumentException::class.java) { CustomGridCellsFixed(0) }
        assertThrows(IllegalArgumentException::class.java) { CustomGridCellsFixed(-1) }
    }

    /**
     * 各画面はrecompositionのたびにインスタンスを生成し直すため、ここが等しくないと
     * remember(columns, ...)が持つ列幅のキャッシュが毎回作り直される。
     */
    @Test
    fun treatsTheSameColumnCountAndEdgeSpaceAsEqual() {
        assertEquals(CustomGridCellsFixed(2, edge.dp), CustomGridCellsFixed(2, edge.dp))
        assertEquals(CustomGridCellsFixed(2, edge.dp).hashCode(), CustomGridCellsFixed(2, edge.dp).hashCode())
        assertEquals(CustomGridCellsFixed(3), CustomGridCellsFixed(3))
    }

    /** 列数か余白が変われば幅も変わるので、キャッシュを引き継いではいけない。 */
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
