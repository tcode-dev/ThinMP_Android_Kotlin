package dev.tcode.thinmp.view.nav

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 遷移のアニメーション中に同じ操作をもう1回しても、遷移は1回だけ起きる。
 * 連打すると、同じ画面が2つ積まれたり、戻るが2回効いて最初の画面まで閉じたりする。
 *
 * アニメーション中の状態を作るため、時計を止めてから2回呼ぶ。
 */
@RunWith(AndroidJUnit4::class)
class NavigatorDoubleTapTest {
    private val timeoutMs = 5_000L
    private val routes = listOf("a", "b", "c")

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: NavHostController
    private lateinit var navigator: Navigator

    @Before
    fun setUp() {
        composeTestRule.setContent {
            navController = rememberNavController()

            NavHost(navController = navController, startDestination = "a") {
                routes.forEach { route -> composable(route) { Text(route) } }
            }
        }

        awaitResumed()
        navigator = Navigator(navController)
    }

    @Test
    fun twoNavigatesDuringTheTransitionAddOneScreen() {
        duringTheTransition({ navigator.navigate("b") }, { navigator.navigate("b") })

        assertEquals(listOf("a", "b"), backStack())
    }

    /**
     * 2回目も効くと、最初の画面まで閉じて何も表示されなくなる。
     */
    @Test
    fun twoBacksDuringTheTransitionCloseOneScreen() {
        onMain { navigator.navigate("b") }
        awaitResumed()

        duringTheTransition({ navigator.back() }, { navigator.back() })

        assertEquals(listOf("a"), backStack())
    }

    /**
     * アニメーションが終われば、次の操作は受け付ける。
     */
    @Test
    fun aNavigateAfterTheTransitionIsAccepted() {
        onMain { navigator.navigate("b") }
        awaitResumed()
        onMain { navigator.navigate("c") }
        awaitResumed()

        assertEquals(listOf("a", "b", "c"), backStack())
    }

    /**
     * イベントで閉じるときは、画面の状態にかかわらず閉じる。
     */
    @Test
    fun backFromEventClosesEvenDuringTheTransition() {
        duringTheTransition({ navigator.navigate("b") }, { navigator.backFromEvent() })

        assertEquals(listOf("a"), backStack())
    }

    /**
     * 時計を止めて1回目を呼び、1フレームだけ進めてから2回目を呼ぶ。
     * そのあと時計を戻し、アニメーションが終わるまで待つ。
     */
    private fun duringTheTransition(first: () -> Unit, second: () -> Unit) {
        composeTestRule.mainClock.autoAdvance = false
        onMain(first)
        composeTestRule.mainClock.advanceTimeByFrame()
        onMain(second)
        composeTestRule.mainClock.autoAdvance = true
        awaitResumed()
    }

    private fun awaitResumed() {
        composeTestRule.waitUntil(timeoutMs) { onMain { navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED } }
    }

    private fun backStack(): List<String> {
        return onMain { navController.currentBackStack.value.mapNotNull { it.destination.route }.filter { it in routes } }
    }

    private fun <T> onMain(block: () -> T): T {
        return composeTestRule.runOnUiThread(block)
    }
}
