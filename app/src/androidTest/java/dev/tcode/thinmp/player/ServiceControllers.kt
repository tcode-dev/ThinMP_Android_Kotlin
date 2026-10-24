package dev.tcode.thinmp.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.test.platform.app.InstrumentationRegistry
import dev.tcode.thinmp.model.media.SongModel
import java.util.concurrent.TimeUnit

/**
 * The tests reach MusicService the way everything else does, through its MediaSession. Each
 * test connects a controller of its own rather than borrowing PlaybackController, which the
 * activity under test connects and releases on its own schedule.
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

/** What PlaybackController.start() sends: no URI, which the service rebuilds in onAddMediaItems(). */
fun startOnController(controller: MediaController, songs: List<SongModel>, index: Int) {
    runOnMain {
        controller.setMediaItems(songs.map { it.toMediaItem(withUri = false) }, index, 0)
        controller.prepare()
        controller.play()
    }
}

/** A controller, and the player behind it, are only ever touched on the main thread. */
fun <T> runOnMain(block: () -> T): T {
    var result: T? = null

    InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }

    @Suppress("UNCHECKED_CAST")
    return result as T
}
