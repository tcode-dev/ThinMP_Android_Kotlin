package dev.tcode.thinmp.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 再生するものがない状態(まだ何もキューに入れていない、またはretry()がキューを空にした)でも、
 * 画面に残ったボタンからの操作はサービスに届く。
 * それでクラッシュしないことを確かめる。
 *
 * 操作はセッション経由で届くので、クラッシュはここには投げられず、テストランナーごとプロセスが落ちる。
 * そのため全操作を送って処理の時間を置き、サービスがまだ応答することで確かめる。
 *
 * 再生はしないので、音声ファイルがなくてもスキップしない。
 */
@RunWith(AndroidJUnit4::class)
class MusicServicePlayerGuardTest {
    private val timeoutMs = 15_000L

    /** 送ったコマンドをセッションが処理し終えるまで待つ時間。 */
    private val settleMs = 1_000L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: MediaController
    private val songRemoved = CountDownLatch(1)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")
        grantPermission("android.permission.POST_NOTIFICATIONS")

        // 再生でサービスがフォアグラウンドになるため、アプリを前面に出しておく。
        // バックグラウンドからのフォアグラウンド化はプラットフォームに拒否される。
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = connectController(context, timeoutMs, object : MediaController.Listener {
            override fun onCustomCommand(controller: MediaController, command: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
                if (command.customAction == MusicService.SONG_REMOVED.customAction) songRemoved.countDown()

                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        })
    }

    @After
    fun tearDown() {
        runOnMain { controller.release() }
        context.stopService(Intent(context, MusicService::class.java))
        scenario.close()
    }

    /** 一度もキューを受け取っていないサービス。 */
    @Test
    fun controlsAreIgnoredBeforeTheFirstStart() {
        assertControlsAreIgnored()
    }

    /**
     * 唯一の曲が失敗し、retry()がキューを空にしてプレーヤーを止めた状態。
     * ミニプレーヤーは消えるが、画面に残ったボタンからの操作は届きうる。
     */
    @Test
    fun controlsAreIgnoredAfterTheQueueEmpties() {
        startOnController(controller, listOf(missingSong("999999998")), 0)

        assertTrue("the missing song was never dropped", songRemoved.await(timeoutMs, TimeUnit.MILLISECONDS))

        assertControlsAreIgnored()
    }

    /** プレーヤー画面とミニプレーヤーが、曲がなくても送れる操作のすべて。 */
    private fun assertControlsAreIgnored() {
        runOnMain {
            controller.play()
            controller.pause()
            controller.seekToNext()
            controller.seekToNextMediaItem()
            controller.seekToPrevious()
            controller.seekToPreviousMediaItem()
            controller.seekTo(1000)
            controller.repeatMode = Player.REPEAT_MODE_ALL
            controller.shuffleModeEnabled = true
        }

        Thread.sleep(settleMs)

        assertTrue("the service went away", runOnMain { controller.isConnected })
        assertEquals("mediaItemCount", 0, runOnMain { controller.mediaItemCount })
        assertNull("currentMediaItem", runOnMain { controller.currentMediaItem })
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
