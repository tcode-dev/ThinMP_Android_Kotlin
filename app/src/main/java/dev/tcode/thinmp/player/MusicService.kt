package dev.tcode.thinmp.player

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.notification.LocalNotificationHelper
import java.io.IOException

interface MusicServiceListener {
    fun onChange() {}
}

class MusicService : Service() {
    private val PREV_MS = 3000
    private val binder = MusicBinder()
    private var player: ExoPlayer? = null
    private lateinit var mediaSession: MediaSession
    private lateinit var mediaStyle: MediaStyleNotificationHelper.MediaStyle
    private var listener: MusicServiceListener? = null
    private var playingList: List<SongModel> = emptyList()
    private lateinit var config: ConfigStore
    private lateinit var repeat: RepeatState
    private var shuffle = false
    private var isPlaying = false

    override fun onCreate() {
        super.onCreate()

        config = ConfigStore(baseContext)
        repeat = config.getRepeat()
        shuffle = config.getShuffle()
    }

    fun addEventListener(listener: MusicServiceListener) {
        this.listener = listener
    }

    fun removeEventListener() {
        this.listener = null
    }

    fun getCurrentSong(): SongModel? {
        if (player?.currentMediaItem == null) return null

        return playingList.first { MediaItem.fromUri(it.getMediaUri()) == player?.currentMediaItem }
    }

    fun start(songs: List<SongModel>, index: Int) {
        playingList = songs
        setPlayer()
        player?.seekTo(index, 0)
        play()
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun prev() {
        if (getCurrentPosition() <= PREV_MS) {
            player?.seekToPrevious()
        } else {
            player?.seekTo(0)
            listener?.onChange()
        }
    }

    fun next() {
        player?.seekToNext()
    }

    fun getRepeat(): RepeatState {
        return repeat
    }

    fun changeRepeat() {
        repeat = when (repeat) {
            RepeatState.OFF -> RepeatState.ALL
            RepeatState.ONE -> RepeatState.OFF
            RepeatState.ALL -> RepeatState.ONE
        }
        setRepeat()
        config.saveRepeat(repeat)
        listener?.onChange()
    }

    fun getShuffle(): Boolean {
        return shuffle
    }

    fun changeShuffle() {
        shuffle = !shuffle
        setShuffle()
        config.saveShuffle(shuffle)
        listener?.onChange()
    }

    fun seekTo(ms: Long) {
        player?.seekTo(ms)
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0
    }

    private fun setRepeat() {
        player?.repeatMode = when (repeat) {
            RepeatState.OFF -> Player.REPEAT_MODE_OFF
            RepeatState.ONE -> Player.REPEAT_MODE_ONE
            RepeatState.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    private fun setShuffle() {
        player?.shuffleModeEnabled = shuffle
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun setPlayer() {
        if (isPlaying) {
            player?.stop()
        }

        try {
            if (player == null) {
                player = ExoPlayer.Builder(baseContext).setLooper(Looper.getMainLooper()).build()
                mediaSession = MediaSession.Builder(baseContext, player!!).build()
                mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
                setRepeat()
                setShuffle()
            }

            val mediaItems = playingList.map {
                MediaItem.fromUri(it.getMediaUri())
            }

            player?.setMediaItems(mediaItems)
            player?.prepare()
            player?.addListener(PlayerEventListener())
        } catch (e: IllegalStateException) {
            println(e)
        }
    }

    inner class PlayerEventListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) return

            if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED) || events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                isPlaying = player.isPlaying
                listener?.onChange()
                notification()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            listener?.onChange()
            notification()
        }
    }

    private fun notification() {
        val song = getCurrentSong() ?: return
        var albumArtBitmap: Bitmap? = null

        try {
            val source = ImageDecoder.createSource(contentResolver, song.getImageUri())

            albumArtBitmap = ImageDecoder.decodeBitmap(source)
        } catch (_: IOException) {
        }

        LocalNotificationHelper.showNotification(baseContext, mediaStyle, song.name, song.artistName, albumArtBitmap)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        if (isPlaying) {
            player?.stop()
        }

        player?.release()
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}