package dev.tcode.thinmp.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.constant.NotificationConstant
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
        println("Log: MusicService onCreate")
        isServiceRunning = true
        config = ConfigStore(baseContext)
        repeat = config.getRepeat()
        shuffle = config.getShuffle()
    }

    fun addEventListener(listener: MusicServiceListener) {
        listeners.add(listener)
    }

    fun removeEventListener(listener: MusicServiceListener) {
        listeners.remove(listener)
    }

    fun getCurrentSong(): SongModel? {
        println("Log: MusicService getCurrentSong 1")
        println("Log: $playingList")
        if (player?.currentMediaItem == null) return null
        println("Log: MusicService getCurrentSong 2")
        return playingList.first { MediaItem.fromUri(it.getMediaUri()) == player?.currentMediaItem }
    }

    fun start(songs: List<SongModel>, index: Int) {
        println("Log: MusicService start 1")
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
            println("Log: MusicService start 2")
            val notification = createNotification()

            LocalNotificationHelper.createNotificationChannel(applicationContext)
            startForeground(NotificationConstant.NOTIFICATION_ID, notification)

            initialized = true
        }
        println("Log: MusicService start 3")
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
            println("Log: MusicService notification IllegalStateException $e")
            player = null

            return false
        }
    }

    inner class PlayerEventListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            for (i in 0 until events.size()) println("Log: MusicService onEvents ${events[i]}")
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) return

            if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED) || events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                isPlaying = player.isPlaying
                onChange()
                notification()
                isPreparing = false
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

            println("Log: MusicService PlayerEventListener onMediaItemTransition")
            onChange()
            notification()
        }

        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
            println("Log: MusicService PlayerEventListener ")
            super.onAudioAttributesChanged(audioAttributes)
        }

        @SuppressLint("UnsafeOptInUsageError")
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            println("Log: MusicService PlayerEventListener onAudioAttributesChanged")
            super.onAudioSessionIdChanged(audioSessionId)
        }

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            println("Log: MusicService PlayerEventListener onAvailableCommandsChanged")
            super.onAvailableCommandsChanged(availableCommands)
        }

        override fun onCues(cueGroup: CueGroup) {
            println("Log: MusicService PlayerEventListener onCues")
            super.onCues(cueGroup)
        }

        override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
            println("Log: MusicService PlayerEventListener onDeviceInfoChanged")
            super.onDeviceInfoChanged(deviceInfo)
        }

        override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
            println("Log: MusicService PlayerEventListener onDeviceVolumeChanged")
            super.onDeviceVolumeChanged(volume, muted)
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            println("Log: MusicService PlayerEventListener onIsLoadingChanged")
            super.onIsLoadingChanged(isLoading)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            println("Log: MusicService PlayerEventListener onIsPlayingChanged")
            super.onIsPlayingChanged(isPlaying)
        }

        override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
            println("Log: MusicService PlayerEventListener onMaxSeekToPreviousPositionChanged")
            super.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            println("Log: MusicService PlayerEventListener onMediaMetadataChanged")
            super.onMediaMetadataChanged(mediaMetadata)
        }

        @SuppressLint("UnsafeOptInUsageError")
        override fun onMetadata(metadata: Metadata) {
            println("Log: MusicService PlayerEventListener onMetadata")
            super.onMetadata(metadata)
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            println("Log: MusicService PlayerEventListener onPlayWhenReadyChanged")
            super.onPlayWhenReadyChanged(playWhenReady, reason)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            println("Log: MusicService PlayerEventListener onPlaybackParametersChanged")
            super.onPlaybackParametersChanged(playbackParameters)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            println("Log: MusicService PlayerEventListener onPlaybackStateChanged")
            super.onPlaybackStateChanged(playbackState)
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            println("Log: MusicService PlayerEventListener onPlaybackSuppressionReasonChanged")
            super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
        }

        override fun onPlayerError(error: PlaybackException) {
            println("Log: MusicService PlayerEventListener onPlayerError")
            super.onPlayerError(error)
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            println("Log: MusicService PlayerEventListener onPlayerErrorChanged")
            super.onPlayerErrorChanged(error)
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            println("Log: MusicService PlayerEventListener onPlaylistMetadataChanged")
            super.onPlaylistMetadataChanged(mediaMetadata)
        }

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            println("Log: MusicService PlayerEventListener onPositionDiscontinuity")
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        }

        override fun onRenderedFirstFrame() {
            println("Log: MusicService PlayerEventListener onRenderedFirstFrame")
            super.onRenderedFirstFrame()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            println("Log: MusicService PlayerEventListener onRepeatModeChanged")
            super.onRepeatModeChanged(repeatMode)
        }

        override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
            println("Log: MusicService PlayerEventListener onSeekBackIncrementChanged")
            super.onSeekBackIncrementChanged(seekBackIncrementMs)
        }

        override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
            println("Log: MusicService PlayerEventListener onSeekForwardIncrementChanged")
            super.onSeekForwardIncrementChanged(seekForwardIncrementMs)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            println("Log: MusicService PlayerEventListener onShuffleModeEnabledChanged")
            super.onShuffleModeEnabledChanged(shuffleModeEnabled)
        }

        override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
            println("Log: MusicService PlayerEventListener onSkipSilenceEnabledChanged")
            super.onSkipSilenceEnabledChanged(skipSilenceEnabled)
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            println("Log: MusicService PlayerEventListener onSurfaceSizeChanged")
            super.onSurfaceSizeChanged(width, height)
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            println("Log: MusicService PlayerEventListener onTimelineChanged")
            super.onTimelineChanged(timeline, reason)
        }

        override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
            println("Log: MusicService PlayerEventListener onTrackSelectionParametersChanged")
            super.onTrackSelectionParametersChanged(parameters)
        }

        override fun onTracksChanged(tracks: Tracks) {
            println("Log: MusicService PlayerEventListener onTracksChanged")
            super.onTracksChanged(tracks)
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            println("Log: MusicService PlayerEventListener onVideoSizeChanged")
            super.onVideoSizeChanged(videoSize)

        }

        override fun onVolumeChanged(volume: Float) {
            println("Log: MusicService PlayerEventListener onVolumeChanged")
            super.onVolumeChanged(volume)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        println("Log: MusicService onLowMemory")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        println("Log: MusicService onTaskRemoved")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        println("Log: MusicService onUnbind")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        println("Log: MusicService onTrimMemory")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        println("Log: MusicService onConfigurationChanged")
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        println("Log: MusicService onConfigurationChanged")
    }

    private fun createNotification(): Notification? {
        val song = getCurrentSong() ?: return null
        var albumArtBitmap: Bitmap? = null

        try {
            val source = ImageDecoder.createSource(contentResolver, song.getImageUri())

            albumArtBitmap = ImageDecoder.decodeBitmap(source)
        } catch (e: IOException) {
            println("Log: MusicService notification IOException $e")
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
            println("Log: MusicService onChange")
            it.onChange()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Log: MusicService onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    @SuppressLint("ServiceCast")
    override fun onDestroy() {
        println("Log: MusicService onDestroy")
        if (isPlaying) {
            playerEventListener?.let { player?.removeListener(it) }
            player?.stop()
        }

        player?.release()
        LocalNotificationHelper.cancelAll(applicationContext)
        stopForeground(STOP_FOREGROUND_DETACH)
        isServiceRunning = false
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}