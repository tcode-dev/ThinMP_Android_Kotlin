package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * The lists key their items by the row's id, so this composition belongs to one row and the
 * dismiss state goes away with the row that was dismissed. Without the item key the row that was
 * below a dismissed one took over its slot and the composition in it, and a state left at
 * DismissedToStart drew that row already swiped off the screen.
 *
 * The item key relies on the same id not appearing twice in one list. Duplicates in a playlist are
 * forbidden, and the favourites and shortcuts are keyed by the id itself, so no list can hold one
 * twice today.
 *
 * The callback is read through rememberUpdatedState because rememberDismissState hands
 * confirmStateChange to the state once, when it is created, and ignores the lambdas that later
 * recompositions pass. The edit screens close over the row's index, and under the item key the
 * composition stays with the row while its index moves up whenever a row above it is dismissed -
 * so the lambda the state was created with names the wrong row by then.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismissView(callback: () -> Unit, content: @Composable RowScope.() -> Unit) {
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
