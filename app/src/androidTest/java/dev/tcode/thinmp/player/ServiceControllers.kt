package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.SongModel
import java.util.concurrent.TimeUnit

/**
 * テストからはMediaSession経由でMusicServiceを操作する。
 * PlaybackControllerは使わず、テストごとにコントローラーを接続する。
 * PlaybackControllerはテスト対象のActivityが接続・解放するため。
 */
fun connectController(context: Context, timeoutMs: Long, listener: MediaController.Listener? = null): MediaController {
    val token = SessionToken(context, ComponentName(context, MusicService::class.java))
    val future = runOnMain {
        val builder = MediaController.Builder(context, token)

        if (listener != null) builder.setListener(listener)

        builder.buildAsync()
    }

    return future.get(timeoutMs, TimeUnit.MILLISECONDS)
}

/** PlaybackController.start()と同じくURIなしで送る。URIはサービスがonAddMediaItems()で補う。 */
fun startOnController(controller: MediaController, songs: List<SongModel>, index: Int) {
    runOnMain {
        controller.setMediaItems(songs.map { it.toMediaItem(withUri = false) }, index, 0)
        controller.prepare()
        controller.play()
    }
}

/** コントローラーとプレーヤーはメインスレッドでのみ操作する。 */
fun <T> runOnMain(block: () -> T): T {
    var result: T? = null

    InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

    @Suppress("UNCHECKED_CAST")
    return result as T
}
