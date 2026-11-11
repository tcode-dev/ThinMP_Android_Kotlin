package dev.tcode.thinmp.view.error

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.tcode.thinmp.R
import dev.tcode.thinmp.activity.HiltTestActivity
import dev.tcode.thinmp.viewModel.ErrorViewModel
import dev.tcode.thinmp.viewModel.MediaStoreFailure
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 包んだ画面のViewModelの代わり。
 * 再読み込みで捨てられたかを確かめる。
 */
class ProbeViewModel : ViewModel() {
    var cleared = false

    override fun onCleared() {
        cleared = true
    }
}

/**
 * MediaStoreの問い合わせが失敗している間は、包んだ画面の代わりにエラーを出す。
 * 再読み込みすると画面を作り直し、前の画面のViewModelも捨てる。
 * NavHostは戻る履歴の画面のViewModelを、LocalViewModelStoreOwnerのストアに置くので、
 * ここでは包んだ画面がそのストアからViewModelを取る形で確かめる。
 */
@RunWith(AndroidJUnit4::class)
class ErrorViewTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private val application: Application = ApplicationProvider.getApplicationContext()
    private val contentText = "thinmp_test_content"

    @Test
    fun aFailureReplacesTheContentAndReloadRebuildsIt() {
        val failure = MediaStoreFailure()
        val viewModel = ErrorViewModel(failure)
        val probes = mutableListOf<ProbeViewModel>()
        val compositions = mutableListOf<Any>()

        composeTestRule.setContent {
            ErrorView(viewModel) {
                val probe = viewModel<ProbeViewModel>()
                val composition = remember { Any() }

                if (probes.lastOrNull() !== probe) probes.add(probe)
                if (compositions.lastOrNull() !== composition) compositions.add(composition)

                Text(contentText)
            }
        }

        composeTestRule.onNodeWithText(contentText).assertExists()

        failure.report()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(contentText).assertDoesNotExist()
        composeTestRule.onNodeWithText(application.getString(R.string.media_store_query_failed)).assertExists()

        composeTestRule.onNodeWithText(application.getString(R.string.reload)).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(contentText).assertExists()
        assertFalse(failure.failed.value)
        assertTrue("the content was not rebuilt", compositions.size == 2)
        assertTrue("the content did not get a new ViewModel", probes.size == 2)
        assertNotSame(probes[0], probes[1])
        assertTrue("the ViewModel of the old content was kept", probes[0].cleared)
        assertFalse(probes[1].cleared)
    }
}
