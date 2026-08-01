package dev.tcode.thinmp.view.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import dev.tcode.thinmp.viewModel.OneShotEvent

@Composable
fun <T> OnEvent(event: OneShotEvent<T>, onEvent: (T) -> Unit) {
    val currentOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(event) {
        event.flow.collect { currentOnEvent(it) }
    }
}
