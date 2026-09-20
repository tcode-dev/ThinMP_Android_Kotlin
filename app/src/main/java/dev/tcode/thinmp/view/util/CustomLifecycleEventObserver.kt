package dev.tcode.thinmp.view.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

interface CustomLifecycleEventObserverListener {
    fun onResume() {}
    fun onStop() {}
    fun onDestroy() {}
}

/**
 * The observer is registered once per lifecycle and reads the listener through
 * rememberUpdatedState, so a recomposition that passes a different listener swaps the target
 * without re-registering. Re-registering is not a no-op: a LifecycleRegistry replays ON_CREATE,
 * ON_START and ON_RESUME to every observer it adds, so an effect keyed on the listener would hand
 * a resumed screen a second ON_RESUME whenever the listener changed.
 */
@Composable
fun CustomLifecycleEventObserver(listener: CustomLifecycleEventObserverListener) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentListener by rememberUpdatedState(listener)

    DisposableEffect(lifecycle) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> currentListener.onResume()
                Lifecycle.Event.ON_STOP -> currentListener.onStop()
                Lifecycle.Event.ON_DESTROY -> currentListener.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}