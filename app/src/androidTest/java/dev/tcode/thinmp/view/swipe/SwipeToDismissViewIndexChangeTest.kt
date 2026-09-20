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
 * Under an item key a row keeps its composition when a row above it is dismissed, and only its
 * index changes. The edit screens close over that index, so the row's callback is a new lambda
 * after every dismissal above it - but rememberDismissState hands confirmStateChange to the state
 * once, when it is created. Unless the view reads the latest callback, the second swipe runs the
 * lambda from before the first and removes the row that is now at that index.
 *
 * The harness is built the way the edit screens are: a keyed list, callbacks that close over the
 * index and nothing else, and rows given the full width because a swipe has to cross half of it
 * to dismiss it. Three rows, so that the stale index names a row that still exists and the
 * failure is the wrong row going rather than an exception.
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

        // b is at index 0 now. A callback kept from its first composition still says 1, which is c.
        composeTestRule.onNodeWithTag("b").performTouchInput { swipeLeft() }
        composeTestRule.waitUntil(timeoutMs) { rows.size == 1 }
        composeTestRule.waitForIdle()

        assertEquals("the row that was swiped is the one removed", listOf("3" to "c"), rows.toList())
    }
}
