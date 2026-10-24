package dev.tcode.thinmp.view.row

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * itemにkeyがないと行は位置で識別されるので、一覧から行が消えると次の行が同じcompositionを使い回し、
 * 消えた行で開いていたメニューが次の行の上に残ってしまう。
 * keyを付ければcompositionは行に結び付き、メニューは開いた行と一緒に破棄される。
 *
 * 画面と同じく、行のidをkeyにした一覧と、長押しで自分のジェスチャー検出からメニューを開く行で確かめる。
 */
@RunWith(AndroidJUnit4::class)
class DropdownMenuViewRowChangeTest {
    private val menuText = "menu"

    @get:Rule
    val composeTestRule = createComposeRule()

    private val rows = mutableStateListOf("1" to "a", "2" to "b")

    @Test
    fun closesTheMenuWhenItsRowIsRemoved() {
        composeTestRule.setContent {
            LazyColumn(Modifier.fillMaxSize()) {
                items(rows, key = { it.first }) { row ->
                    DropdownMenuView(dropdownContent = { Text(menuText) }) { callback ->
                        Text(row.second, Modifier
                            .testTag(row.second)
                            .pointerInput(Unit) { detectTapGestures(onLongPress = { callback() }) })
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("a").performTouchInput { longClick() }
        composeTestRule.onNodeWithText(menuText).assertIsDisplayed()

        // メニューを開いた行を消し、次の行が繰り上がる。
        rows.removeAt(0)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(menuText).assertDoesNotExist()
        composeTestRule.onNodeWithTag("b").assertIsDisplayed()
    }
}
