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
import androidx.media3.common.PlaybackException
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
    fun onEnded() {}
    fun onError() {}
}

class MusicService : Service() {
    private val PREV_MS = 3000
    private val binder = MusicBinder()
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    @SuppressLint("UnsafeOptInUsageError")
    private lateinit var mediaStyle: MediaStyleNotificationHelper.MediaStyle
    private lateinit var headsetEventReceiver: HeadsetEventReceiver
    private lateinit var playerEventListener: PlayerEventListener
    private lateinit var config: ConfigStore
    private lateinit var repeat: RepeatState
    private var listeners: MutableList<MusicServiceListener> = mutableListOf()
    private var playingList: List<SongModel> = emptyList()
    private var initialized: Boolean = false
    private var shuffle = false
    private var isPlaying = false
    private var isStarting = false

    // Serviceの起動状態を確認する必要がある
    // Android13以降を対象にしているのでgetRunningServicesやLocalBroadcastManagerは使用できない
    // そのためcompanion objectでServiceの起動状態を管理する
    // アプリを再起動してもisServiceRunningは前回起動時の値のままなのでonDestroyで初期化する
    companion object {
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()

        isServiceRunning = true
        config = ConfigStore(baseContext)
        repeat = config.getRepeat()
        shuffle = config.getShuffle()
        headsetEventReceiver = HeadsetEventReceiver { player.stop() }

        registerReceiver(headsetEventReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
        initPlayer()
    }

    fun addEventListener(listener: MusicServiceListener) {
        listeners.add(listener)
    }

    fun removeEventListener(listener: MusicServiceListener) {
        listeners.remove(listener)
    }

    fun getCurrentSong(): SongModel? {
        if (player.currentMediaItem == null) return null

        return playingList.first { MediaItem.fromUri(it.getMediaUri()) == player.currentMediaItem }
    }

    fun start(songs: List<SongModel>, index: Int) {
        if (isStarting) return

        isStarting = true
        playingList = songs

        setPlayer(index)
        play()
        startFirstService()
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun prev() {
        if (getCurrentPosition() <= PREV_MS) {
            player.seekToPrevious()
        } else {
            player.seekTo(0)
            onChange()
        }
    }

    fun next() {
        player.seekToNext()
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
        player.seekTo(ms)
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initPlayer() {
        player = ExoPlayer.Builder(applicationContext).setLooper(Looper.getMainLooper()).build()
        mediaSession = MediaSession.Builder(applicationContext, player).build()
        mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
        setRepeat()
        setShuffle()
    }

    private fun setPlayer(index: Int) {
        if (isPlaying) {
            player.stop()
        }

        val mediaItems = playingList.map {
            MediaItem.fromUri(it.getMediaUri())
        }

        player.setMediaItems(mediaItems)
        player.prepare()
        player.seekTo(index, 0)
        playerEventListener = PlayerEventListener()
        player.addListener(playerEventListener)
    }

    private fun startFirstService() {
        if (initialized) return

        val notification = createNotification()

        LocalNotificationHelper.createNotificationChannel(applicationContext)
        startForeground(NotificationConstant.NOTIFICATION_ID, notification)

        initialized = true
    }

    private fun setRepeat() {
        player.repeatMode = when (repeat) {
            RepeatState.OFF -> Player.REPEAT_MODE_OFF
            RepeatState.ONE -> Player.REPEAT_MODE_ONE
            RepeatState.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    private fun setShuffle() {
        player.shuffleModeEnabled = shuffle
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

    private fun onEnded() {
        listeners.forEach {
            it.onEnded()
        }
    }

    private fun onError() {
        listeners.forEach {
            it.onError()
        }
    }

    private fun retry() {
        val count = playingList.count()
        val currentIndex = player.currentMediaItemIndex
        val list = playingList.toMutableList()

        list.removeAt(currentIndex)
        player.release()
        mediaSession.release()
        initPlayer()

        if (list.isNotEmpty()) {
            val nextIndex = if (count == currentIndex + 1) currentIndex -1 else currentIndex

            start(list, nextIndex)
        } else {
            isStarting = false
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
            player.stop()
        }

        player.removeListener(playerEventListener)
        player.release()
        mediaSession.release()
        LocalNotificationHelper.cancelAll(applicationContext)
        unregisterReceiver(headsetEventReceiver)
        stopForeground(STOP_FOREGROUND_DETACH)
        isServiceRunning = false
    }

    inner class PlayerEventListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) return

            if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED) || events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                isPlaying = player.isPlaying
                onChange()
                notification()
                isStarting = false
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            onChange()
            notification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                isPlaying = false
                player.pause()
                player.seekTo(0, 0)
                onEnded()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            // 曲が削除されている場合
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                retry()
                onError()
            } else {
                isStarting = false
            }
        }
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}