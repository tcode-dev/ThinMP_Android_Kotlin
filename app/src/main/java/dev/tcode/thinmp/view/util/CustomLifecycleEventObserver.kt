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
 * observerはlifecycleごとに1回だけ登録し、リスナーはrememberUpdatedStateで最新のものを読む。
 * リスナーをkeyにして登録し直すと、LifecycleRegistryが追加時にON_CREATE、ON_START、
 * ON_RESUMEを送り直すので、resume済みの画面に2回目のON_RESUMEが届いてしまう。
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