package dev.tcode.thinmp.player

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
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
import androidx.media3.common.Player.PositionInfo
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
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

    // player.isPlayingはseekbarを操作中falseになる
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
        isPlaying = true
    }

    fun play() {
        try {
            player?.play()
        } catch (e: IllegalStateException) {
            fix()
        }
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

        if (isPlaying) {
            play()
        }
    }

    fun next() {
        player?.seekToNext()

        if (isPlaying) {
            play()
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

        config.saveRepeat(repeat)
    }

    fun getShuffle(): Boolean {
        return shuffle
    }

    fun changeShuffle() {
        shuffle = !shuffle
        setShuffle()
        config.saveShuffle(shuffle)
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
            println("exoPlayer onMediaItemTransition")
            listener?.onChange()
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            println("exoPlayer onTimelineChanged")
        }

        override fun onTracksChanged(tracks: Tracks) {
            println("exoPlayer onTracksChanged")
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            println("exoPlayer onMediaMetadataChanged")
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            println("exoPlayer onPlaylistMetadataChanged")
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            println("exoPlayer onIsLoadingChanged")
        }

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            println("exoPlayer onAvailableCommandsChanged")
        }

        override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
            println("exoPlayer onTrackSelectionParametersChanged")
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            println("exoPlayer onTrackSelectionParametersChanged")
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            println("exoPlayer onPlayWhenReadyChanged")
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            println("exoPlayer onPlaybackSuppressionReasonChanged")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            println("exoPlayer onIsPlayingChanged: $isPlaying")
//                listener?.onChange(isPlaying)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            println("exoPlayer onRepeatModeChanged")
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            println("exoPlayer onShuffleModeEnabledChanged")
        }

        override fun onPlayerError(error: PlaybackException) {
            println("exoPlayer onPlayerError")
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            println("exoPlayer onPlayerErrorChanged")
        }

        override fun onPositionDiscontinuity(oldPosition: PositionInfo, newPosition: PositionInfo, reason: Int) {
            println("exoPlayer onPositionDiscontinuity")
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            println("exoPlayer onPlaybackParametersChanged")
        }

        override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
            println("exoPlayer onSeekBackIncrementChanged")
        }

        override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
            println("exoPlayer onSeekForwardIncrementChanged")
        }

        override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
            println("exoPlayer onMaxSeekToPreviousPositionChanged")
        }

        @UnstableApi
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            println("exoPlayer onAudioSessionIdChanged")
        }

        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
            println("exoPlayer onAudioAttributesChanged")
        }

        override fun onVolumeChanged(volume: Float) {
            println("exoPlayer onVolumeChanged")
        }

        override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
            println("exoPlayer onSkipSilenceEnabledChanged")
        }

        override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
            println("exoPlayer onDeviceInfoChanged")
        }

        override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
            println("exoPlayer onDeviceVolumeChanged")
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            println("exoPlayer onVideoSizeChanged")
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            println("exoPlayer onSurfaceSizeChanged")
        }

        override fun onRenderedFirstFrame() {
            println("exoPlayer onRenderedFirstFrame")
        }

        override fun onCues(cueGroup: CueGroup) {
            println("exoPlayer onCues")
        }

        @UnstableApi
        override fun onMetadata(metadata: Metadata) {
            println("exoPlayer onMetadata")
        }
    }

    private fun fix() {
//        if (playingList.hasNext()) {
//            setPlayer(playingList.next())
//            play()
//            listener?.onChange()
//        }
    }

    private fun notification() {
        val song = getCurrentSong() ?: return
        var albumArtBitmap: Bitmap? = null

        try {
            val source = ImageDecoder.createSource(contentResolver, song.getImageUri())

            albumArtBitmap = ImageDecoder.decodeBitmap(source)
        } catch (e: IOException) {
        }

        LocalNotificationHelper.showNotification(baseContext, mediaStyle, song.name, song.artistName, albumArtBitmap)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        if (player?.isPlaying == true) {
            player?.stop()
        }

        player?.release()
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}