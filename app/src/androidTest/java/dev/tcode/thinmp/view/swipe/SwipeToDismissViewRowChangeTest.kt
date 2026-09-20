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
 * A dismissed row is removed from the list. Without an item key the row below it took over the
 * slot - and the composition in it, the dismiss state included - and, left at DismissedToStart,
 * that state drew the new row already swiped off the screen. The item key is what keeps the
 * composition with the row: the dismissed row's state is disposed of with it, and the row that
 * moves up keeps its own.
 *
 * The harness is built the way the edit screens are: a list keyed by the row's id, callbacks that
 * close over the index and nothing else, and rows given the full width because a swipe has to
 * cross half of it to dismiss it.
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
                itemsIndexed(rows) { index, row ->
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
