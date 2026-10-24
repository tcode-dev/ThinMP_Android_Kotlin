package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * スワイプで消した行は一覧から取り除かれる。
 * itemにkeyがないと、下の行がその位置のcompositionをdismissのstateごと引き継ぎ、
 * DismissedToStartのままのstateのせいで、新しい行が最初から画面外にスワイプされた状態で描かれてしまう。
 * keyを付ければ、消した行のstateは行と一緒に破棄され、繰り上がった行は自分のstateを保つ。
 *
 * 編集画面と同じく、行のidをkeyにした一覧、インデックスだけを取り込むコールバック、
 * 全幅の行で確かめる(swipeLeftは行の右端から左端までを200msで動かすので、行が狭いと消すのに要る56dpにも125dp/sにも届かない)。
 */
@RunWith(AndroidJUnit4::class)
class SwipeToDismissViewRowChangeTest {
    private val timeoutMs = 5_000L
    private val toleranceDp = 0.5f

    @get:Rule
    val composeTestRule = createComposeRule()

    private val rows = mutableStateListOf("1" to "a", "2" to "b")

    @Test
    fun keepsTheNextRowInPlaceWhenARowIsDismissed() {
        composeTestRule.setContent {
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(rows, key = { _, row -> row.first }) { index, row ->
                    SwipeToDismissView(callback = { rows.removeAt(index) }) {
                        Text(row.second, Modifier
                            .fillMaxWidth()
                            .testTag(row.second))
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("a").performTouchInput { swipeLeft() }
        composeTestRule.waitUntil(timeoutMs) { rows.size == 1 }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("b").assertIsDisplayed()
        assertEquals("the row that moved up is drawn swiped away", 0f, composeTestRule.onNodeWithTag("b").getUnclippedBoundsInRoot().left.value, toleranceDp)
    }
}
