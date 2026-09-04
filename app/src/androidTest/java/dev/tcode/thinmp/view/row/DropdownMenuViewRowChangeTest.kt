package dev.tcode.thinmp.view.row

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The lists have no item key, so a row is identified by its position: a list that loses an entry
 * leaves the next row in that slot, reusing the composition. The open/closed state has to go with
 * the row it was opened on rather than with the slot, or the menu is left standing over whatever
 * moved up into it - and it is the id that says which of the two happened.
 *
 * Changing the row's content alone is not enough to make this test say anything: the content
 * reaches the view as a composable lambda, and a change to what it captures recomposes the lambda
 * without recomposing DropdownMenuView around it. Passing the id as an ordinary parameter is what
 * makes the view itself recompose here.
 */
@RunWith(AndroidJUnit4::class)
class DropdownMenuViewRowChangeTest {
    private val rowTag = "row"
    private val menuText = "menu"

    @get:Rule
    val composeTestRule = createComposeRule()

    private val id = mutableStateOf("1")
    private val label = mutableStateOf("before")

    @Test
    fun closesTheMenuWhenTheRowIsReplaced() {
        composeTestRule.setContent {
            val text = label.value

            DropdownMenuView(id = id.value, dropdownContent = { Text(menuText) }) {
                Text(text, Modifier.testTag(rowTag))
            }
        }

        composeTestRule.onNodeWithTag(rowTag).performClick()
        composeTestRule.onNodeWithText(menuText).assertIsDisplayed()

        // The row the menu was opened on is gone and the next one has taken the slot.
        id.value = "2"
        label.value = "after"
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(menuText).assertDoesNotExist()
    }
}
