package dev.tcode.thinmp.player

import android.content.Context
import android.os.Bundle
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * MediaControllerのバインドで起動したサービスは、最後のバインドが外れると破棄される。
 * MediaSessionServiceは再生が始まるまで自分をstartしないので、
 * 読み込み中や読み込みに失敗した曲は接続と一緒に消えてしまう。
 * 再生は、それを始めた画面より長く続かなければならない。
 *
 * 存在しない曲を使うので、音声ファイルがなくてもスキップしない。
 */
@RunWith(AndroidJUnit4::class)
class MusicServiceLifetimeTest {
    private val timeoutMs = 15_000L

    /**
     * 起きてはいけない破棄を待つ時間。
     */
    private val quietMs = 1_000L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        // バックグラウンドからはサービスを起動できないため、アプリを前面に出しておく。
        scenario = ActivityScenario.launch(MainActivity::class.java)
        // Activityの接続があるとそれだけでサービスが生き残るので、解放しておく。
        runOnMain { PlaybackController.from(context).release() }
    }

    @After
    fun tearDown() {
        scenario.close()
        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
    }

    @Test
    fun theServiceOutlivesTheConnectionThatQueuedASong() {
        val songRemoved = CountDownLatch(1)
        val controller = connectController(context, timeoutMs, object : MediaController.Listener {
            override fun onCustomCommand(controller: MediaController, command: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
                if (command.customAction == MusicService.SONG_REMOVED.customAction) songRemoved.countDown()

                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        })

        startOnController(controller, listOf(missingSong("999999997")), 0)
        // 読み込み失敗の通知は、サービスがキューを受け取った証拠になる。
        // 再生が一度も始まらないケースでもある。
        assertTrue("the missing song was never dropped", songRemoved.await(timeoutMs, TimeUnit.MILLISECONDS))

        val watcher = MusicServiceWatcher.attach(context, timeoutMs)

        runOnMain { controller.release() }

        assertFalse("the service went away with the connection", watcher.awaitDestroyed(quietMs))
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
