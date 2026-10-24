package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
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
 * 1回のスワイプでコールバックは1回だけ呼ばれる。
 * 編集画面のコールバックは行のインデックスを消すので、2回呼ばれると下の行まで消えてしまう。
 *
 * 行を消すコールバックでは、1回目で行のcompositionが破棄され、残りの呼び出しがキャンセルされて見えなくなることがある。
 * そのため、行を残したまま、アニメーションが終わるまで待って回数を数える。
 */
@RunWith(AndroidJUnit4::class)
class SwipeToDismissViewCallbackCountTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun callsTheCallbackOncePerSwipe() {
        var count = 0
        composeTestRule.setContent {
            SwipeToDismissView(callback = { count++ }) {
                Text("a", Modifier
                    .fillMaxWidth()
                    .testTag("a"))
            }
        }

        composeTestRule.onNodeWithTag("a").performTouchInput { swipeLeft() }
        composeTestRule.waitForIdle()

        assertEquals("callback invocations", 1, count)
    }
}
