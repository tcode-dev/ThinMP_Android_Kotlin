package dev.tcode.thinmp.application

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.MusicServiceWatcher
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MainApplication.onActivityDestroyed()はActivityの破棄でサービスを止めるが、
 * 構成変更(ダークモード、フォントサイズ、ロケール)による再生成では止めてはいけない。
 * 止めるとアプリを表示したまま再生が止まる。
 *
 * 構成変更の時点でPlaybackControllerの接続はonStop()で解放済みなので、サービスはバインドされていない。
 * テストも同じ状態を作る。
 * バインドを持ったままだとサービスはそれだけで生き残り、テストが常に通ってしまう。
 *
 * 確認するのは「インスタンスが破棄されないこと」。
 * 再生成されたActivityはBIND_AUTO_CREATEで再接続するので、
 * 止まったサービスもすぐ新しいインスタンスで起動し、
 * 動いているかどうかでは区別できない(失われるのは古いインスタンスのキューだけ)。
 *
 * 再生はしないので、音声ファイルがなくてもスキップしない。
 */
@RunWith(AndroidJUnit4::class)
class MainApplicationConfigChangeTest {
    private val timeoutMs = 15_000L

    /** 起きてはいけない破棄を待つ時間。 */
    private val quietMs = 1_000L

    private lateinit var context: Context
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        scenario = ActivityScenario.launch(MainActivity::class.java)
        context.startService(Intent(context, MusicService::class.java))
    }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        context.stopService(Intent(context, MusicService::class.java))
    }

    @Test
    fun theServiceSurvivesAConfigurationChange() {
        val watcher = MusicServiceWatcher.attach(context, timeoutMs)

        scenario!!.recreate()

        assertFalse("the configuration change stopped the service", watcher.awaitDestroyed(quietMs))
    }

    /** 本当にアプリを離れたときは止まること。 */
    @Test
    fun theServiceStopsWhenTheActivityGoesAway() {
        val watcher = MusicServiceWatcher.attach(context, timeoutMs)

        scenario!!.close()
        scenario = null

        // stopService()は非同期なので、onDestroy()はclose()の後に来る。
        assertTrue("the service outlived the activity", watcher.awaitDestroyed(timeoutMs))
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
