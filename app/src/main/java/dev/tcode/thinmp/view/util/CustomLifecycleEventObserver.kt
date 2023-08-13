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

@Composable
fun CustomLifecycleEventObserver(listener: CustomLifecycleEventObserverListener) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnEvent by rememberUpdatedState(listener)

    DisposableEffect(key1 = lifecycle, key2 = currentOnEvent) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> listener.onResume()
                Lifecycle.Event.ON_STOP -> listener.onStop()
                Lifecycle.Event.ON_DESTROY -> listener.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}