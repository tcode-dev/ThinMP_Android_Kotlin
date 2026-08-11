package dev.tcode.thinmp.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.constant.NotificationConstant
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.notification.LocalNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

interface MusicServiceListener {
    fun onChange() {}
    fun onError() {}
}

class MusicService : Service() {
    private val PREV_MS = 3000
    private val ALBUM_ART_MAX_PX = 512
    private val binder = MusicBinder()

    /**
     * Null until start() builds one and again from release() on, so "never started" and "already
     * released" are the same state rather than a lateinit check and a flag that each covered only
     * one of them. Every caller has to go through `?.` or an early return, which is what keeps the
     * transport controls from reaching a player that is not there.
     */
    private var player: ExoPlayer? = null
    private lateinit var mediaSession: MediaSession
    @SuppressLint("UnsafeOptInUsageError")
    private lateinit var mediaStyle: MediaStyleNotificationHelper.MediaStyle
    private lateinit var playerEventListener: PlayerEventListener
    private lateinit var config: ConfigStore

    /**
     * Owns the ConfigStore reads and writes and the album art decode, so none of them run on the
     * main thread. Cancelled in onDestroy().
     */
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)

    /**
     * Defaults until the stored values arrive. onCreate() used to block the main thread on two
     * DataStore reads, inside the five second window startForegroundService() allows.
     */
    private var repeat: RepeatState = RepeatState.OFF
    private var notificationJob: Job? = null
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

        loadConfig()
    }

    fun addEventListener(listener: MusicServiceListener) {
        listeners.add(listener)
    }

    fun removeEventListener(listener: MusicServiceListener) {
        listeners.remove(listener)
    }

    /**
     * firstOrNull, not first: retry() swaps playingList for a shorter one while the old player is
     * still winding down, so the current item briefly belongs to a list this one no longer holds.
     */
    fun getCurrentSong(): SongModel? {
        val currentMediaItem = player?.currentMediaItem ?: return null

        return playingList.firstOrNull { MediaItem.fromUri(it.getMediaUri()) == currentMediaItem }
    }

    fun start(songs: List<SongModel>, index: Int) {
        if (isStarting) return

        isStarting = true
        playingList = songs

        release()
        setPlayer(index)
        play()
        startFirstService()
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun prev() {
        val player = this.player ?: return

        if (player.currentPosition <= PREV_MS) {
            if (isFirstSong(player)) {
                seekToLast(player)
            } else {
                player.seekToPrevious()
            }
        } else {
            player.seekTo(0)
            onChange()
        }
    }

    fun next() {
        val player = this.player ?: return

        if (isLastSong(player)) {
            seekToFirst(player)
        } else {
            player.seekToNext()
        }
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
        player?.let { setRepeat(it) }
        onChange()
        // Reads the field at execution time rather than capturing it, so rapid taps all persist
        // the state the user actually ended on.
        serviceScope.launch { config.saveRepeat(repeat) }
    }

    fun getShuffle(): Boolean {
        return shuffle
    }

    fun changeShuffle() {
        shuffle = !shuffle
        player?.let { setShuffle(it) }
        onChange()
        serviceScope.launch { config.saveShuffle(shuffle) }
    }

    fun seekTo(ms: Long) {
        val player = this.player ?: return

        try {
            player.seekTo(ms)
        } catch (e: Exception) {
            onError()
        }
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0
    }

    /**
     * setHandleAudioBecomingNoisy replaces a HEADSET_PLUG receiver this service used to register in
     * onCreate(). That receiver called player.stop() on a player that onCreate() has not built yet
     * and that release() may already have freed, it stopped rather than paused - which leaves
     * ExoPlayer idle, so the play button afterwards did nothing until the queue was rebuilt - and
     * it read the wired headset's state extra with AudioManager's Bluetooth SCO constants, which
     * only lined up because both happen to be 0. ACTION_AUDIO_BECOMING_NOISY is the intent meant for
     * this, it covers Bluetooth going away as well as the wired jack, and the player enables and
     * disables its own receiver around playback, so there is no window where a released player is
     * reachable.
     */
    @OptIn(UnstableApi::class)
    private fun setPlayer(index: Int) {
        val player = ExoPlayer.Builder(applicationContext).setLooper(Looper.getMainLooper()).setHandleAudioBecomingNoisy(true).build()

        this.player = player
        mediaSession = MediaSession.Builder(applicationContext, player).build()
        mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)

        setRepeat(player)
        setShuffle(player)

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

        LocalNotificationHelper.createNotificationChannel(applicationContext)
        // Posted without art so startForeground() lands well inside the five second deadline
        // startForegroundService() sets. buildNotification() never returns null, which the
        // previous code could when the current song was not resolvable yet.
        startForeground(NotificationConstant.NOTIFICATION_ID, buildNotification(getCurrentSong(), null))

        initialized = true

        notification()
    }

    /**
     * The stored values arrive after onCreate() has returned. If the player already exists by
     * then they are applied straight away; otherwise setPlayer() reads the fields when it runs.
     */
    private fun loadConfig() {
        serviceScope.launch {
            repeat = config.getRepeat()
            shuffle = config.getShuffle()

            val player = this@MusicService.player ?: return@launch

            setRepeat(player)
            setShuffle(player)
            onChange()
        }
    }

    private fun setRepeat(player: ExoPlayer) {
        player.repeatMode = when (repeat) {
            RepeatState.OFF -> Player.REPEAT_MODE_OFF
            RepeatState.ONE -> Player.REPEAT_MODE_ONE
            RepeatState.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    private fun setShuffle(player: ExoPlayer) {
        player.shuffleModeEnabled = shuffle
    }

    private fun isFirstSong(player: ExoPlayer): Boolean {
        return player.currentMediaItemIndex == 0
    }

    private fun isLastSong(player: ExoPlayer): Boolean {
        return player.currentMediaItemIndex == player.mediaItemCount - 1
    }

    private fun seekToFirst(player: ExoPlayer) {
        player.seekTo(0, 0)
    }

    private fun seekToLast(player: ExoPlayer) {
        player.seekTo(player.mediaItemCount - 1, 0)
    }

    /** Pure assembly, no I/O, so it is safe to call while holding up the main thread. */
    private fun buildNotification(song: SongModel?, albumArt: Bitmap?): Notification {
        return LocalNotificationHelper.createNotification(
            applicationContext, mediaStyle, song?.name ?: "", song?.artistName ?: "", albumArt
        )
    }

    /**
     * Embedded album art is regularly several megapixels and this used to be decoded at full size
     * on the main thread, once per track change. The notification only ever shows a small icon.
     */
    private suspend fun decodeAlbumArt(song: SongModel): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val source = ImageDecoder.createSource(contentResolver, song.getImageUri())

            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.setTargetSampleSize(sampleSize(info.size))
            }
        } catch (_: IOException) {
            null
        }
    }

    private fun sampleSize(size: Size): Int {
        val longestEdge = maxOf(size.width, size.height)
        var sampleSize = 1

        while (longestEdge / (sampleSize * 2) >= ALBUM_ART_MAX_PX) {
            sampleSize *= 2
        }

        return sampleSize
    }

    /** Re-posts the notification once the art for [song] is decoded. A track change cancels the
     * decode still in flight, so the art can never belong to the previous song. */
    private fun notification() {
        val song = getCurrentSong() ?: return

        notificationJob?.cancel()
        notificationJob = serviceScope.launch {
            val albumArt = decodeAlbumArt(song)

            LocalNotificationHelper.notify(buildNotification(song, albumArt), applicationContext)
        }
    }

    private fun onChange() {
        listeners.forEach {
            it.onChange()
        }
    }

    private fun onError() {
        retry()
        listeners.forEach {
            it.onError()
        }
    }

    /**
     * Drops the song that failed and starts again on the next one.
     *
     * The start this is recovering from is over before we get here: playback never began, so the
     * EVENT_IS_PLAYING_CHANGED that would have cleared isStarting is never coming. Clearing it
     * here rather than only on the empty-list path is what lets the start() below run at all -
     * it returns immediately while the flag is set, which left the service holding a released
     * player and a flag nothing would ever clear again, so every later start() returned too and
     * playback was dead until the service was destroyed.
     */
    private fun retry() {
        val player = this.player ?: return
        val count = playingList.count()
        val currentIndex = player.currentMediaItemIndex
        val list = playingList.toMutableList()

        list.removeAt(currentIndex)

        release()
        isStarting = false

        if (list.isEmpty()) return

        val nextIndex = if (count == currentIndex + 1) currentIndex - 1 else currentIndex

        start(list, nextIndex)
    }

    /**
     * Guarded on the player rather than on `initialized`, which is only set once startForeground
     * has run: between setPlayer() and startFirstService() a player exists that the old guard
     * would have skipped. Clearing the field makes this idempotent, so retry() calling release()
     * and then start() calling it again no longer reaches into an already released ExoPlayer.
     */
    private fun release() {
        val player = this.player ?: return

        if (isPlaying) {
            player.stop()
        }

        player.removeListener(playerEventListener)
        player.release()
        mediaSession.release()
        this.player = null
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    @SuppressLint("ServiceCast")
    override fun onDestroy() {
        // Was a copy of release() without its guard, so a service destroyed before start() ever
        // ran crashed on the uninitialised player.
        serviceJob.cancel()
        release()
        LocalNotificationHelper.cancelAll(applicationContext)
        stopForeground(STOP_FOREGROUND_DETACH)
        isServiceRunning = false
    }

    inner class PlayerEventListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) return

            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                isPlaying = player.isPlaying
                onChange()
                isStarting = false
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            onChange()
            notification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            // ループ再生していない場合最後の曲の再生が終了すると呼ばれる
            // 曲が1曲の場合、onMediaItemTransition、events.contains(Player.EVENT_IS_PLAYING_CHANGED)は呼ばれない
            if (playbackState == Player.STATE_ENDED) {
                val player = this@MusicService.player ?: return

                isPlaying = false
                player.pause()
                seekToFirst(player)
                onChange()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            // 曲が削除されている場合
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
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