package dev.tcode.thinmp.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.session.PlaybackState
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
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
import dev.tcode.thinmp.R
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel

interface MusicServiceListener {
    fun onChange() {}
}

class MusicService : Service() {
    private val PREV_MS = 3000
    private val binder = MusicBinder()
    private lateinit var notificationManager: NotificationManager
    private var exoPlayer: ExoPlayer? = null
    private lateinit var mediaSession: MediaSession
    private lateinit var mediaStyle: MediaStyleNotificationHelper.MediaStyle
    private lateinit var pendingIntentList: PendingIntent
    private lateinit var playbackState: PlaybackState.Builder
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
        if (exoPlayer?.currentMediaItem == null) return null

        return playingList.first { MediaItem.fromUri(it.getMediaUri()) == exoPlayer?.currentMediaItem }
    }

    fun start(songs: List<SongModel>, index: Int) {
        playingList = songs
        setRepeat()
        setShuffle()
        setExoPlayer()
        exoPlayer?.seekTo(index, 0)
        play()
        isPlaying = true
        LocalNotificationHelper.showNotification(baseContext, "TestTitle", "TestMessage")
    }

    fun play() {
        try {
            exoPlayer?.play()
        } catch (e: IllegalStateException) {
            fix()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun prev() {
        val isContinue = exoPlayer?.isPlaying

        if (getCurrentPosition() <= PREV_MS) {
            exoPlayer?.seekToPrevious()
        } else {
            exoPlayer?.seekTo(0)
            listener?.onChange()
        }

        if (isContinue == true) {
            play()
        }
    }

    fun next() {
        val isContinue = exoPlayer?.isPlaying

        exoPlayer?.seekToNext()

        if (isContinue == true) {
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
        exoPlayer?.seekTo(ms)
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    private fun setRepeat() {
        exoPlayer?.repeatMode = when (repeat) {
            RepeatState.OFF -> Player.REPEAT_MODE_OFF
            RepeatState.ONE -> Player.REPEAT_MODE_ONE
            RepeatState.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    private fun setShuffle() {
        exoPlayer?.shuffleModeEnabled = shuffle
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun setExoPlayer() {
        destroy()

        try {
            exoPlayer = ExoPlayer.Builder(baseContext).setLooper(Looper.getMainLooper()).build()
            val mediaItems = playingList.map {
                MediaItem.fromUri(it.getMediaUri())
            }
            exoPlayer?.setMediaItems(mediaItems)
            exoPlayer?.prepare()
            mediaSession = MediaSession.Builder(baseContext, exoPlayer!!).build()
            playbackState = PlaybackState.Builder()
            mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession)
            mediaStyle.setShowActionsInCompactView(0, 1, 2)

            addListener()
        } catch (e: IllegalStateException) {
            println(e)
        }
    }

    private fun addListener() {
        exoPlayer?.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                val size = events.size()

                for (index in 0 until size) {
                    println("exoPlayer onEvents $index:${index + 1}=${events[index]}")
                }
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) return

                if (events.contains(Player.EVENT_MEDIA_METADATA_CHANGED) || events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                    isPlaying = player.isPlaying
                    listener?.onChange()
                    val intent = Intent("SEND_MESSAGE")
                    intent.putExtra("MUSIC", "FWD")
//                    Localbroadcastmanager.getInstance(baseContext).sendBroadcast(intent)
                }
                println("exoPlayer onEvents end")
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                println("exoPlayer onMediaItemTransition")
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                println("exoPlayer onTimelineChanged")
            }

            override fun onTracksChanged(tracks: Tracks) {
                println("exoPlayer onTracksChanged")
//                listener?.onChange(exoPlayer!!.isPlaying)
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
        })
    }

    private fun destroy() {
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.stop()
        }

        exoPlayer?.release()
    }

//    private fun createCompletionListener(): OnCompletionListener {
//        return OnCompletionListener {
//            if (repeat == RepeatState.ONE) {
//                song?.let { setExoPlayer(it) }
//                play()
//            } else {
//                if (playingList.hasNext()) {
//                    setExoPlayer(playingList.next())
//                    play()
//                } else {
//                    playingList = if (shuffle) {
//                        shuffledList.listIterator(0)
//                    } else {
//                        originalList.listIterator(0)
//                    }
//                    setExoPlayer(playingList.next())
//                    if (repeat == RepeatState.ALL) {
//                        play()
//                    }
//                }
//            }
//
//            listener?.onChange()
//        }
//    }

    private fun fix() {
//        if (playingList.hasNext()) {
//            setExoPlayer(playingList.next())
//            play()
//            listener?.onChange()
//        }
    }

    // bindした時点でexoplayerをsetする必要がありそう
    override fun onBind(intent: Intent): IBinder {
        pendingIntentList = PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        notificationManager = baseContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(TAG, TAG, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Silent Notification"
        channel.setSound(null, null)
        channel.enableLights(false)
        channel.lightColor = Color.BLUE
        channel.enableVibration(false)
        if (notificationManager != null) {
            println("notificationManager ある")
        } else {
            println("notificationManager ない")
        }
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(baseContext, "1").setContentTitle(baseContext.getString(R.string.app_name)).setContentText(TAG).setContentIntent(pendingIntentList)
//            .setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.round_favorite_24).setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.round_favorite_24)).setStyle(mediaStyle)
////                .addAction(NotificationCompat.Action(R.drawable.round_favorite_24, RWD, CustomMediaButtonReceiver.buildMediaButtonPendingIntent(baseContext, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))
////                .addAction(
////                    if (exoPlayer?.isPlaying == false) NotificationCompat.Action(
////                        R.drawable.round_favorite_24,
////                        PLAY,
////                        CustomMediaButtonReceiver.buildMediaButtonPendingIntent(baseContext, PlaybackStateCompat.ACTION_PLAY)
////                    ) else NotificationCompat.Action(R.drawable.ic_round_pause, PAUSE, CustomMediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE))
////                )
////                .addAction(NotificationCompat.Action(R.drawable.ic_round_fwd, FWD, CustomMediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
            .build()
//        notificationManager.notify(1, notification)
//        startForeground(1, notification)
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}