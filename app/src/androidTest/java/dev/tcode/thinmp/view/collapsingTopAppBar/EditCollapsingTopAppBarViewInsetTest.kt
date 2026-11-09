package dev.tcode.thinmp.view.collapsingTopAppBar

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.nav.INavigator
import dev.tcode.thinmp.view.nav.LocalNavigator
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 編集画面の一覧を最後までスクロールすると、最後の行はナビゲーションバーより上に来る。
 * アプリはシステムバーの下まで描くので、一覧の最後に空きがないと最後の行がナビゲーションバーに隠れる。
 *
 * MainActivityと同じく、システムバーの下まで描く状態にしてから表示する。
 */
@RunWith(AndroidJUnit4::class)
class EditCollapsingTopAppBarViewInsetTest {
    private val rowCount = 40

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theLastRowEndsAboveTheNavigationBar() {
        composeTestRule.runOnUiThread { WindowCompat.setDecorFitsSystemWindows(composeTestRule.activity.window, false) }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigator provides NoNavigator) {
                EditCollapsingTopAppBarView(enabled = true, callback = {}) {
                    items(rowCount) { Text("row $it", Modifier.height(StyleConstant.ROW_HEIGHT.dp)) }
                }
            }
        }

        val navigationBar = composeTestRule.runOnUiThread {
            ViewCompat.getRootWindowInsets(composeTestRule.activity.window.decorView)?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        }
        assumeTrue("the device has no navigation bar at the bottom", navigationBar > 0)

        // 最後の行まで送ってから、末尾にある空きの分もスワイプして一覧の終わりまで動かす。
        // 末尾の空きの有無で項目の数が変わるので、項目の位置では終わりを指定しない。
        val list = composeTestRule.onNode(hasScrollToIndexAction())

        list.performScrollToIndex(rowCount)
        repeat(3) { list.performTouchInput { swipeUp() } }
        composeTestRule.waitForIdle()

        val rootHeight = with(composeTestRule.density) { composeTestRule.onRoot().getBoundsInRoot().bottom.toPx() }
        val lastRowBottom = with(composeTestRule.density) { composeTestRule.onNodeWithText("row ${rowCount - 1}").getBoundsInRoot().bottom.toPx() }

        assertTrue(
            "the last row ends at $lastRowBottom px, under the navigation bar that starts at ${rootHeight - navigationBar} px",
            lastRowBottom <= rootHeight - navigationBar + 1
        )
    }

    private object NoNavigator : INavigator {
        override fun back() {}
        override fun backFromEvent() {}
        override fun navigate(route: String) {}
        override fun mainEdit() {}
        override fun artistDetail(id: String) {}
        override fun albumDetail(id: String) {}
        override fun favoriteArtistsEdit() {}
        override fun favoriteSongsEdit() {}
        override fun playlistsEdit() {}
        override fun playlistDetail(id: String) {}
        override fun playlistDetailEdit(id: String) {}
        override fun player() {}
    }
}
