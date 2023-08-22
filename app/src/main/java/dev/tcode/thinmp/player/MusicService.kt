package dev.tcode.thinmp.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
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
import dev.tcode.thinmp.constant.NotificationConstant
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.notification.LocalNotificationHelper
import dev.tcode.thinmp.receiver.HeadsetEventReceiver
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
    private lateinit var headsetEventReceiver: HeadsetEventReceiver
    private var listeners: MutableList<MusicServiceListener> = mutableListOf()
    private var playerEventListener: PlayerEventListener? = null
    private var playingList: List<SongModel> = emptyList()
    private lateinit var config: ConfigStore
    private lateinit var repeat: RepeatState
    private var initialized: Boolean = false
    private var shuffle = false
    private var isPlaying = false

    companion object {
        var isServiceRunning = false
        var isPreparing = false
    }

    override fun onCreate() {
        super.onCreate()

        isServiceRunning = true
        config = ConfigStore(baseContext)
        repeat = config.getRepeat()
        shuffle = config.getShuffle()
        headsetEventReceiver = HeadsetEventReceiver { player?.stop() }
        registerReceiver(headsetEventReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
    }

    fun addEventListener(listener: MusicServiceListener) {
        listeners.add(listener)
    }

    fun removeEventListener(listener: MusicServiceListener) {
        listeners.remove(listener)
    }

    fun getCurrentSong(): SongModel? {
        if (player?.currentMediaItem == null) return null

        return playingList.first { MediaItem.fromUri(it.getMediaUri()) == player?.currentMediaItem }
    }

    fun start(songs: List<SongModel>, index: Int) {
        isPreparing = true
        playingList = songs

        val result = setPlayer()

        if (!result) {
            isPreparing = false
            return
        }

        player?.seekTo(index, 0)
        play()

        if (!initialized) {
            val notification = createNotification()

            LocalNotificationHelper.createNotificationChannel(applicationContext)
            startForeground(NotificationConstant.NOTIFICATION_ID, notification)

            initialized = true
        }
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
            onChange()
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
        onChange()
    }

    fun getShuffle(): Boolean {
        return shuffle
    }

    fun changeShuffle() {
        shuffle = !shuffle
        setShuffle()
        config.saveShuffle(shuffle)
        onChange()
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
    private fun setPlayer(): Boolean {
        if (isPlaying) {
            player?.stop()
        }

        try {
            if (player == null) {
                player = ExoPlayer.Builder(applicationContext).setLooper(Looper.getMainLooper()).build()
                mediaSession = MediaSession.Builder(applicationContext, player!!).build()
                mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
                setRepeat()
                setShuffle()
            }

            val mediaItems = playingList.map {
                MediaItem.fromUri(it.getMediaUri())
            }

            player?.setMediaItems(mediaItems)
            player?.prepare()
            playerEventListener = PlayerEventListener()
            player?.addListener(playerEventListener!!)

            return true
        } catch (e: IllegalStateException) {
            player = null

            return false
        }
    }

    inner class PlayerEventListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) return

            if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED) || events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                isPlaying = player.isPlaying
                onChange()
                notification()
                isPreparing = false
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            onChange()
            notification()
        }
    }

    private fun createNotification(): Notification? {
        val song = getCurrentSong() ?: return null
        var albumArtBitmap: Bitmap? = null

        try {
            val source = ImageDecoder.createSource(contentResolver, song.getImageUri())

            albumArtBitmap = ImageDecoder.decodeBitmap(source)
        } catch (_: IOException) {
        }

        return LocalNotificationHelper.createNotification(applicationContext, mediaStyle, song.name, song.artistName, albumArtBitmap)
    }

    private fun notification() {
        val notification = createNotification()

        if (notification != null) {
            LocalNotificationHelper.notify(notification, applicationContext)
        }
    }

    private fun onChange() {
        listeners.forEach {
            it.onChange()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    @SuppressLint("ServiceCast")
    override fun onDestroy() {
        if (isPlaying) {
            playerEventListener?.let { player?.removeListener(it) }
            player?.stop()
        }

        player?.release()
        LocalNotificationHelper.cancelAll(applicationContext)
        stopForeground(STOP_FOREGROUND_DETACH)
        unregisterReceiver(headsetEventReceiver)
        isServiceRunning = false
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}