package dev.tcode.thinmp.view.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

interface CustomLifecycleEventObserverListener {
    fun onResume(context: Context) {}
    fun onStop(context: Context) {}
    fun onDestroy(context: Context) {}
}

@Composable
fun CustomLifecycleEventObserver(listener: CustomLifecycleEventObserverListener) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnEvent by rememberUpdatedState(listener)
    val applicationContext = LocalContext.current.applicationContext

    DisposableEffect(key1 = lifecycle, key2 = currentOnEvent) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> listener.onResume(applicationContext)
                Lifecycle.Event.ON_STOP -> listener.onStop(applicationContext)
                Lifecycle.Event.ON_DESTROY -> listener.onDestroy(applicationContext)
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}