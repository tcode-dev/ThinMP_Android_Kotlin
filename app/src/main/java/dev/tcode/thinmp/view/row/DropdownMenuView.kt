package dev.tcode.thinmp.view.row

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * The lists key their items by the row's id, so this composition belongs to one row and is
 * disposed of with it: the open/closed state and the gesture detector need no key of their own.
 * Without the item key a list that lost an entry left the next row in the slot the old one had,
 * reusing the composition - and a menu left open from the old row then stood over whatever moved
 * up into it.
 *
 * The row is what opens the menu, from its own gesture detector, which is why `content` is handed
 * the callback. A detector here would never fire: the row's detectTapGestures consumes the down
 * event, and a detector on the parent waits for one that is not consumed.
 */
@Composable
fun DropdownMenuView(dropdownContent: @Composable ColumnScope.(callback: () -> Unit) -> Unit, content: @Composable BoxScope.(callback: () -> Unit) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        val expanded = remember { mutableStateOf(false) }
        val callback = { expanded.value = !expanded.value }

        Box {
            content(callback)
            DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), modifier = Modifier.background(MaterialTheme.colorScheme.onBackground), onDismissRequest = callback) {
                dropdownContent(callback)
            }
        }
    }
}
