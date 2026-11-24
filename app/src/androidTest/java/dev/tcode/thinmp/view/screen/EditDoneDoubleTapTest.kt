package dev.tcode.thinmp.view.screen

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.R
import dev.tcode.thinmp.activity.HiltTestActivity
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.nav.Navigator
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 編集画面で完了を押し、閉じる遷移のアニメーション中にもう一度押しても、閉じるのは編集画面だけ。
 * アニメーション中も編集画面は描かれていてタップを受け付け、OnEventも残っている。
 * 2回目の保存で保存済みがもう一度届くと、backFromEvent()が前の画面まで閉じてしまう。
 *
 * アニメーション中の状態を作るため、時計を止めてから1回目を押し、遷移が始まるまでフレームを進める。
 *
 * 保存するのは読み込んだ一覧そのものなので、端末のプレイリストは変わらない。
 */
@RunWith(AndroidJUnit4::class)
class EditDoneDoubleTapTest {
    private val timeoutMs = 10_000L
    private val maxFrames = 500
    private val start = "start"

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private lateinit var application: Application
    private lateinit var navController: NavHostController
    private lateinit var done: String

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        done = application.getString(R.string.done)

        composeTestRule.setContent {
            navController = rememberNavController()

            CompositionLocalProvider(LocalNavigator provides Navigator(navController)) {
                NavHost(navController = navController, startDestination = start) {
                    composable(start) { Text(start) }
                    composable(NavConstant.PLAYLISTS_EDIT) { PlaylistsEditScreen() }
                }
            }
        }

        awaitResumed()
        composeTestRule.runOnUiThread { navController.navigate(NavConstant.PLAYLISTS_EDIT) }
        awaitResumed()
    }

    @Test
    fun tappingDoneTwiceClosesOnlyTheEditScreen() {
        awaitDoneEnabled()

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onNodeWithText(done).performClick()
        advanceUntil { currentRoute() == start }

        // 遷移の途中なので、編集画面はまだ描かれている。
        assertEquals(1, composeTestRule.onAllNodesWithText(done).fetchSemanticsNodes().size)

        composeTestRule.onNodeWithText(done).performClick()
        // 2回目の保存が終わり、保存済みが届くだけの時間を進める。
        repeat(60) {
            composeTestRule.mainClock.advanceTimeByFrame()
            Thread.sleep(10)
        }

        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        assertEquals(listOf(start), backStack())
    }

    /**
     * 完了ボタンは読み込みが終わるまで無効で、押しても何も起きない。
     */
    private fun awaitDoneEnabled() {
        composeTestRule.waitUntil(timeoutMs) {
            composeTestRule.onAllNodesWithText(done).fetchSemanticsNodes().firstOrNull()?.config?.contains(SemanticsProperties.Disabled) == false
        }
    }

    /**
     * 時計を止めたまま1フレームずつ進め、条件を満たすまで待つ。
     * 保存はRoomのスレッドで進むので、フレームの間に実時間も少し待つ。
     */
    private fun advanceUntil(condition: () -> Boolean) {
        repeat(maxFrames) {
            if (composeTestRule.runOnUiThread(condition)) return

            composeTestRule.mainClock.advanceTimeByFrame()
            Thread.sleep(10)
        }

        throw AssertionError("condition was not met within $maxFrames frames")
    }

    private fun awaitResumed() {
        composeTestRule.waitUntil(timeoutMs) { composeTestRule.runOnUiThread { navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED } }
    }

    private fun currentRoute(): String? {
        return navController.currentBackStackEntry?.destination?.route
    }

    private fun backStack(): List<String> {
        return composeTestRule.runOnUiThread { navController.currentBackStack.value.mapNotNull { it.destination.route }.filter { it == start || it == NavConstant.PLAYLISTS_EDIT } }
    }
}
