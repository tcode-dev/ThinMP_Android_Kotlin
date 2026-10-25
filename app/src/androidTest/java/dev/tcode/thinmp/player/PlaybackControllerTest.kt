package dev.tcode.thinmp.player

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.activity.MainActivity
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.viewModel.MiniPlayerUiState
import dev.tcode.thinmp.viewModel.MiniPlayerViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 画面がサービスを操作する窓口であるPlaybackControllerのテスト。
 * アプリと同じくActivityが接続する。
 *
 * どのテストも実在の曲を再生するので、端末に音声ファイルが必要で、
 * なければスキップする(tools/push-test-audio.sh)。
 */
@RunWith(AndroidJUnit4::class)
class PlaybackControllerTest {
    private val timeoutMs = 15_000L
    private val pollMs = 50L

    private lateinit var context: Context
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var controller: PlaybackController
    private lateinit var song: SongModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        grantPermission("android.permission.READ_MEDIA_AUDIO")

        val songs = runBlocking { SongRepository(context).findAll() }
        assumeTrue("needs at least one audio file in MediaStore", songs.isNotEmpty())
        song = songs.first()

        MusicServiceWatcher.stopAndAwait(context, timeoutMs)
        // 再生でサービスがフォアグラウンドになるため、アプリを前面に出しておく。
        // バックグラウンドからのフォアグラウンド化はプラットフォームに拒否される。
        scenario = ActivityScenario.launch(MainActivity::class.java)
        controller = PlaybackController.from(context)
        await("the controller never connected") { onMain { controller.isConnected() } }
    }

    @After
    fun tearDown() {
        onMain { controller.pause() }
        scenario.close()
        context.stopService(Intent(context, MusicService::class.java))
    }

    /**
     * コントローラーはURIなしで曲を送るので、サービスがonAddMediaItems()でURIを補わないと再生できない。
     */
    @Test
    fun playsASongStartedThroughTheController() {
        onMain { controller.start(listOf(song), 0) }

        awaitPlaying()
    }

    /**
     * 未接続の間(onStop()から次のonStart()まで、または最初の接続の完了前)に呼ばれたstartも、
     * 捨てずに接続後に実行する。
     */
    @Test
    fun runsAStartMadeBeforeTheConnection() {
        onMain {
            controller.release()
            controller.start(listOf(song), 0)
            controller.connect()
        }

        awaitPlaying()
    }

    /**
     * 曲の再生後に開いた画面(子画面)のミニプレーヤーは、バインドを待たずに現在の状態を表示する。
     */
    @Test
    fun aNewMiniPlayerHasTheCurrentSongFromTheStart() {
        onMain { controller.start(listOf(song), 0) }
        awaitPlaying()

        val store = ViewModelStore()

        try {
            val uiState = onMain {
                val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context as Application)

                ViewModelProvider(store, factory)[MiniPlayerViewModel::class.java].uiState.value
            }

            // テスト用の曲は2秒しかないので、再生中かどうかは比較しない。
            assertEquals(MiniPlayerUiState(song.name, song.getImageUri(), isVisible = true), uiState.copy(isPlaying = false))
        } finally {
            onMain { store.clear() }
        }
    }

    private fun awaitPlaying() {
        await("the song never started playing") {
            onMain { controller.state.value.let { it.isPlaying && it.currentSong?.id == song.id } }
        }
    }

    private fun await(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs

        while (!condition()) {
            assertTrue(message, SystemClock.uptimeMillis() < deadline)
            Thread.sleep(pollMs)
        }
    }

    /**
     * コントローラーはメインスレッドで動くので、操作はすべてメインスレッドで行う。
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
