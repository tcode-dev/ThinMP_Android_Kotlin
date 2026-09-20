package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState

/**
 * `id` is the row's identity, and it keys the dismiss state so that the state is reset when the
 * slot starts showing a different row and only then. The lists key their items by the same id,
 * so a row keeps its composition and a removed row takes its state away with it; keying on the id
 * here as well is what keeps this view right on its own, in a list without an item key, where the
 * row that was below a dismissed one takes over its slot and the composition in it - a state left
 * at DismissedToStart draws that row already swiped off the screen.
 *
 * Passing the id as an ordinary parameter is half of what makes that work. The row itself arrives
 * as a composable lambda, so without the id a change of row would recompose the lambda alone and
 * this view would keep the state of the row that has gone.
 *
 * The callback is read through rememberUpdatedState because rememberDismissState hands
 * confirmStateChange to the state once, when it is created, and ignores the lambdas that later
 * recompositions pass. The edit screens close over the row's index, and under an item key the
 * composition stays with the row while its index moves up whenever a row above it is dismissed -
 * so the lambda the state was created with names the wrong row by then.
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
        val currentCallback by rememberUpdatedState(callback)
        val dismissState = rememberDismissState(confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                currentCallback()
                true
            } else {
                false
            }
        })
        SwipeToDismiss(state = dismissState, directions = setOf(DismissDirection.EndToStart), background = {}, dismissContent = content)
    }
}
