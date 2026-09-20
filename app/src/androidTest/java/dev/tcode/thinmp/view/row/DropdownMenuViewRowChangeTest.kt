package dev.tcode.thinmp.view.row

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
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
 * Without an item key a row is identified by its position: a list that loses an entry leaves the
 * next row in that slot, reusing the composition, and a menu opened on the old row is left
 * standing over whatever moved up into it. The item key keeps the composition with the row, so
 * the open menu is disposed of together with the row it was opened on.
 *
 * The harness is a list keyed by the row's id, as the screens are.
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
                items(rows) { row ->
                    DropdownMenuView(dropdownContent = { Text(menuText) }) {
                        Text(row.second, Modifier.testTag(row.second))
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("a").performClick()
        composeTestRule.onNodeWithText(menuText).assertIsDisplayed()

        // The row the menu was opened on is gone and the next one has moved up.
        rows.removeAt(0)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(menuText).assertDoesNotExist()
        composeTestRule.onNodeWithTag("b").assertIsDisplayed()
    }
}
