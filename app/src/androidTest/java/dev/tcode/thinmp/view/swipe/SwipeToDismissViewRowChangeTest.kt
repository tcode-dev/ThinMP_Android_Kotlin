package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
 * A dismissed row is removed from the list, and the row below it takes over the slot - and the
 * composition in it, the dismiss state included. Left at DismissedToStart, that state draws the new
 * row already swiped off the screen. The id is what resets it: the state belongs to the row rather
 * than to the position.
 *
 * The harness is built the way the edit screens are, and both halves of that matter. The rows are a
 * plain Column, so a row is identified by its position exactly as it is in a list with no item key.
 * The callback closes over the index and nothing else, so it is the same lambda before and after
 * the removal - one that closed over the id instead would recompose the view on its own and hide
 * what the id is being passed for. The row is given the full width because a swipe has to cross
 * half of it to dismiss it.
 */
@RunWith(AndroidJUnit4::class)
class SwipeToDismissViewRowChangeTest {
    private val timeoutMs = 5_000L
    private val toleranceDp = 0.5f

    @get:Rule
    val composeTestRule = createComposeRule()

    private val rows = mutableStateListOf("1" to "a", "2" to "b")

    @Test
    fun resetsTheStateWhenTheRowIsReplaced() {
        composeTestRule.setContent {
            Column {
                rows.forEachIndexed { index, row ->
                    SwipeToDismissView(row.first, callback = { rows.removeAt(index) }) {
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
