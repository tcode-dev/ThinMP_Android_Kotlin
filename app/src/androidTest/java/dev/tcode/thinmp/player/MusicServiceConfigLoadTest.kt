package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * onCreate()はDataStoreの読み込みを待たないので、
 * 読み込みが終わるまでリピートとシャッフルは既定値のまま、ボタンは操作できる状態にある。
 * その間のタップが、後から届いた保存値で上書きされないことを確かめる。
 *
 * MediaControllerでバインドするだけなので、再生もstartForeground()も起きず、音声ファイルも要らない。
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceConfigLoadTest {
    /**
     * タップが読み込みより先に届くかどうかは制御できない。
     * アプリからのタップが届きうる最も早い時点(接続コールバック)で送るが、
     * 読み込みに負けた試行は何も検証できないだけなので、複数回試す。
     * 上書きのバグは1回でも起きれば検出できる。
     */
    private val attempts = 5
    private val timeoutMs = 15_000L
    private val quietMs = 1_000L

    private lateinit var context: Context
    private lateinit var config: ConfigStore
    private var controller: MediaController? = null

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        config = ConfigStore(context)

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        resetConfig()
    }

    @After
    fun tearDown() = runBlocking {
        unbind()
        resetConfig()
    }

    /**
     * 保存値はONEとfalse、タップはALLとtrueなので、読み込みがタップを上書きした場合だけ結果が変わる。
     *
     * リピートの保存値を既定値(OFF)とも違う値にして、タップを送る時点でまだ読み込みが反映されていなかったかを記録する。
     * 接続コールバックでコントローラーがOFFを示していれば、タップは読み込みより先に送られている。
     *
     * サービスはテストと同じプロセスで動くので、DataStoreはすでに読み込み済みで、data.first()は中断せずに値を返す。
     * そのためloadConfig()はonCreate()の中で終わり、タップが先に届く試行は普通は起きない。
     * そのような試行が1回もなければ何も確かめていないので、通さずにスキップする。
     */
    @Test
    fun tappingBeforeTheConfigLoadsKeepsTheTap() = runBlocking {
        var tappedBeforeLoad = 0

        for (attempt in 1..attempts) {
            storeConfig()

            var beforeLoad = false
            val controller = connect {
                beforeLoad = it.repeatMode == Player.REPEAT_MODE_OFF
                it.repeatMode = Player.REPEAT_MODE_ALL
                it.shuffleModeEnabled = true
            }

            delay(quietMs)

            assertEquals("the stored repeat overwrote the tap on attempt $attempt", Player.REPEAT_MODE_ALL, onMain { controller.repeatMode })
            assertTrue("the stored shuffle overwrote the tap on attempt $attempt", onMain { controller.shuffleModeEnabled })

            // 接続コールバックはメインスレッドで動くので、メインスレッドで読めばその後になる。
            if (onMain { beforeLoad }) tappedBeforeLoad++

            unbind()
        }

        assumeTrue("the config loaded before the tap on every attempt, so nothing was checked", tappedBeforeLoad > 0)
    }

    /**
     * タップがなければ保存値が適用されること。
     */
    @Test
    fun theStoredValuesAreAppliedWhenNothingIsTapped() = runBlocking {
        config.saveRepeat(RepeatState.ONE)
        config.saveShuffle(true)

        val controller = connect {}

        await("the stored repeat was never applied") { onMain { controller.repeatMode } == Player.REPEAT_MODE_ONE }
        await("the stored shuffle was never applied") { onMain { controller.shuffleModeEnabled } }
    }

    private suspend fun storeConfig() {
        config.saveRepeat(RepeatState.ONE)
        config.saveShuffle(false)

        assertEquals("the stored repeat was not saved", RepeatState.ONE, config.getRepeat())
        assertFalse("the stored shuffle was not saved", config.getShuffle())
    }

    private suspend fun resetConfig() {
        config.saveRepeat(RepeatState.OFF)
        config.saveShuffle(false)
    }

    /**
     * [onConnected]はメインスレッドの接続コールバック内で呼ばれる。
     */
    private fun connect(onConnected: (MediaController) -> Unit): MediaController {
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        // 作成とリスナー登録を同じメインスレッドのターンで行うので、
        // リスナー登録前にfutureが完了することはない。
        val future = onMain {
            MediaController.Builder(context, token).buildAsync().also { future ->
                future.addListener({ onConnected(future.get()) }, MoreExecutors.directExecutor())
            }
        }

        val controller = future.get(timeoutMs, TimeUnit.MILLISECONDS)

        this.controller = controller

        return controller
    }

    /**
     * 接続の解放だけでなく、サービスの破棄まで待つ。
     * 待たないと、次の接続に破棄途中の古いインスタンス(設定を読み込み済み)が応答してしまう。
     */
    private fun unbind() {
        val controller = this.controller ?: return

        this.controller = null
        onMain { controller.release() }
        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
    }

    private suspend fun await(message: String, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, System.currentTimeMillis() < deadline)
            delay(20)
        }
    }

    /**
     * フィールドはサービスのメインスレッドのコルーチンが書くので、読むのもメインスレッドで行う。
     */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }
}
