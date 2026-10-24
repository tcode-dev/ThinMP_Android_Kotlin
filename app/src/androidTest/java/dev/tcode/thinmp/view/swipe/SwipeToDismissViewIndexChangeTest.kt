package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
 * itemにkeyがあると、上の行を消しても下の行はcompositionを保ち、インデックスだけが変わる。
 * 編集画面のコールバックはインデックスを取り込んだラムダなので、そのたびに新しくなる。
 * 行を作ったときのコールバックを使い続けると、2回目のスワイプで古いインデックスの行が消えてしまう。
 *
 * 編集画面と同じく、keyを付けた一覧、インデックスだけを取り込むコールバック、
 * 全幅の行で確かめる(swipeLeftは行の右端から左端までを200msで動かすので、行が狭いと消すのに要る56dpにも125dp/sにも届かない)。
 * 行を3つにして、古いインデックスでも存在する行を指すようにし、
 * 失敗が例外ではなく「違う行が消える」として現れるようにしている。
 */
@RunWith(AndroidJUnit4::class)
class SwipeToDismissViewIndexChangeTest {
    private val timeoutMs = 5_000L

    @get:Rule
    val composeTestRule = createComposeRule()

    private val rows = mutableStateListOf("1" to "a", "2" to "b", "3" to "c")

    @Test
    fun removesTheRowAtItsCurrentIndex() {
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
        composeTestRule.waitUntil(timeoutMs) { rows.size == 2 }
        composeTestRule.waitForIdle()

        // bはいまインデックス0。最初のcompositionのコールバックのままなら1(=c)を消してしまう。
        composeTestRule.onNodeWithTag("b").performTouchInput { swipeLeft() }
        composeTestRule.waitUntil(timeoutMs) { rows.size == 1 }
        composeTestRule.waitForIdle()

        assertEquals("the row that was swiped is the one removed", listOf("3" to "c"), rows.toList())
    }
}
