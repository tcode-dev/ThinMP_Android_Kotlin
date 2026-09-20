package dev.tcode.thinmp.view.util

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A LifecycleRegistry replays ON_CREATE, ON_START and ON_RESUME to every observer it adds. The
 * listener is therefore read through the observer rather than being a key of the effect: an effect
 * keyed on the listener would re-register whenever a recomposition passed a different one, and the
 * replay would hand a screen that is already resumed a second ON_RESUME. The new listener has to
 * receive the events that follow the swap all the same.
 */
@RunWith(AndroidJUnit4::class)
class CustomLifecycleEventObserverTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private class CountingListener : CustomLifecycleEventObserverListener {
        var resumed = 0
        var stopped = 0

        override fun onResume() {
            resumed++
        }

        override fun onStop() {
            stopped++
        }
    }

    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = registry
    }

    @Test
    fun swappingTheListenerDoesNotReplayOnResume() {
        val first = CountingListener()
        val second = CountingListener()
        val listener = mutableStateOf<CustomLifecycleEventObserverListener>(first)
        lateinit var owner: Owner

        composeTestRule.runOnUiThread {
            owner = Owner()
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                CustomLifecycleEventObserver(listener.value)
            }
        }

        assertEquals("the registration replays the current state", 1, first.resumed)

        listener.value = second
        composeTestRule.waitForIdle()

        assertEquals("a second ON_RESUME reached the screen", 1, first.resumed)
        assertEquals("the swap replayed ON_RESUME to the new listener", 0, second.resumed)

        composeTestRule.runOnUiThread {
            owner.registry.currentState = Lifecycle.State.CREATED
        }

        assertEquals("ON_STOP went to the listener that was replaced", 0, first.stopped)
        assertEquals("ON_STOP did not reach the new listener", 1, second.stopped)
    }
}
