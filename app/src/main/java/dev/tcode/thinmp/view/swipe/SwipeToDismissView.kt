package dev.tcode.thinmp.view.swipe

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import java.util.UUID

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismissView(callback: () -> Unit, content: @Composable RowScope.() -> Unit) {
    key(UUID.randomUUID()) {
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