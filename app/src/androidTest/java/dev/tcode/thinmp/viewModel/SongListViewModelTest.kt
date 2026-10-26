package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.player.MusicService
import dev.tcode.thinmp.player.MusicServiceWatcher
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.repository.SongRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 何もクエリせず、再読み込みの回数だけを数える。
 * ファクトリーから作れるよう、publicでApplicationを受け取る。
 */
class CountingSongListViewModel(application: Application) : SongListViewModel(application) {
    var loads = 0

    override val songs: List<SongModel> = emptyList()

    override fun load() {
        loads++
    }
}

/**
 * 曲リストはサービスにバインドせずPlaybackControllerを読む。
 * 再生できない曲が外されたら再読み込みし、コントローラーが持つ現在の状態からミニプレーヤーを表示する。
 * アプリと同じくActivityが接続する。
 */
@RunWith(AndroidJUnit4::class)
class SongListViewModelTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: PlaybackController
    private val store = ViewModelStore()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        // 再生でサービスがフォアグラウンドになるため、アプリを前面に出しておく。
        // バックグラウンドからのフォアグラウンド化はプラットフォームに拒否される。
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = PlaybackController.from(context)
        await("the controller never connected") { onMain { controller.isConnected() } }
    }

    @After
    fun tearDown() {
        onMain {
            store.clear()
            controller.pause()
        }
        scenario.close()
        context.stopService(Intent(context, MusicService::class.java))
    }

    /**
     * 実在の曲は再生しないので、音声ファイルがなくてもスキップしない。
     */
    @Test
    fun reloadsWhenTheServiceDropsASong() {
        val viewModel = onMain { create(CountingSongListViewModel::class.java) }

        onMain { controller.start(listOf(missingSong("999999998")), 0) }

        await("the list was not reloaded after the song was dropped") { onMain { viewModel.loads } > 0 }
    }

    /**
     * 曲がキューにあるときに初めて開いた一覧(子画面など)は、最初からミニプレーヤーを表示する。
     * 端末に音声ファイルが必要で、なければスキップする(tools/push-test-audio.sh)。
     */
    @Test
    fun aListOpenedWhileASongIsQueuedShowsTheMiniPlayerFromTheStart() {
        val song = runBlocking { SongRepository(context).findAll() }.firstOrNull()
        assumeTrue("needs at least one audio file in MediaStore", song != null)

        onMain { controller.start(listOf(song!!), 0) }
        await("the song never reached the queue") { onMain { controller.state.value.hasQueue } }

        val isVisible = onMain { create(SongsViewModel::class.java).isVisiblePlayer.value }

        assertTrue("the mini player was hidden on the list's first frame", isVisible)
    }

    /**
     * 画面と同じく、メインスレッドで作る。
     */
    private fun <T : ViewModel> create(modelClass: Class<T>): T {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context as Application)

        return ViewModelProvider(store, factory)[modelClass]
    }

    private fun missingSong(id: String): SongModel {
        return SongModel(SongId(id), "missing", ArtistId(""), "", AlbumId(""), "", 0, "")
    }

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, SystemClock.uptimeMillis() < deadline)
            Thread.sleep(pollMs)
        }
    }

    /**
     * コントローラーとview modelはメインスレッドで動くので、操作はすべてメインスレッドで行う。
     */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun grantPermission(permission: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, permission)
    }
}
