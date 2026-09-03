package dev.tcode.thinmp.view.row

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * `id` is the row's identity, and it keys both the gesture detector and the open/closed state, so
 * that they are thrown away when the slot starts showing a different row and only then. The lists
 * have no item key, so a list that loses an entry leaves the next row in the slot the old one had,
 * reusing the composition - and a menu left open from the old row then stands over whatever moved
 * up into it.
 *
 * Passing the id as an ordinary parameter is half of what makes that work. The row arrives here as
 * a composable lambda, and a change to what that lambda captures recomposes the lambda without
 * recomposing this view around it, so the new row alone would never reach the state held here.
 *
 * The key it replaces was a fresh UUID, which says nothing about the row: it discards the detector
 * whenever this view happens to recompose, and never when the row behind it changed.
 */
@Composable
fun DropdownMenuView(id: String, dropdownContent: @Composable ColumnScope.(callback: () -> Unit) -> Unit, content: @Composable BoxScope.(callback: () -> Unit) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        val expanded = remember(id) { mutableStateOf(false) }
        val callback = { expanded.value = !expanded.value }

        Box(Modifier.pointerInput(id) {
            detectTapGestures(onLongPress = { callback() }, onTap = { callback() })
        }) {
            content(callback)
            DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), modifier = Modifier.background(MaterialTheme.colorScheme.onBackground), onDismissRequest = callback) {
                dropdownContent(callback)
            }
        }
    }
}
