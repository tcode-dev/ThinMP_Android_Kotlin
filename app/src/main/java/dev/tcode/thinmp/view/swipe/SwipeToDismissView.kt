package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

/**
 * `id` is the row's identity, and it keys the dismiss state so that the state is reset when the
 * slot starts showing a different row and only then. The lists have no item key, so the row that
 * was below a dismissed one takes over its slot and the composition in it: a state left at
 * DismissedToStart draws that row already swiped off the screen.
 *
 * Passing the id as an ordinary parameter is half of what makes that work. The callbacks the edit
 * screens pass close over the index, which does not change when the list shrinks under them, and
 * the row itself arrives as a composable lambda - so without the id this view is skipped entirely
 * and keeps the state of the row that has gone.
 *
 * The key it replaces was a fresh UUID, which says nothing about the row: it discards the state
 * whenever this view happens to recompose, mid-swipe included, and never when the row behind it
 * changed.
 *
 * This relies on the same id not appearing twice in one list. Duplicates in a playlist are
 * forbidden, and the favourites and shortcuts are keyed by the id itself, so no list can hold one
 * twice today.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismissView(id: String, callback: () -> Unit, content: @Composable RowScope.() -> Unit) {
    key(id) {
        val dismissState = rememberDismissState(confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                callback()
                true
            } else {
                false
            }
        })
        SwipeToDismiss(state = dismissState, directions = setOf(DismissDirection.EndToStart), background = {}, dismissContent = content)
    }
}
