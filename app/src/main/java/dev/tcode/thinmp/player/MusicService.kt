package dev.tcode.thinmp.player

//import android.media.MediaPlayer
//import android.media.MediaPlayer.OnCompletionListener

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
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
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel

interface MusicServiceListener {
    fun onChange() {}
}

@UnstableApi class MusicService : Service() {
    private val PREV_MS = 3000
    private val INTERVAL_MS = 1000L
    private val binder = MusicBinder()
    private val handler = Handler(Looper.getMainLooper())

    //    private var mediaPlayer: MediaPlayer? = null
    private var exoPlayer: ExoPlayer? = null
    private lateinit var mediaSession: MediaSession
    private var listener: MusicServiceListener? = null
    private var playingList: List<SongModel> = emptyList()

    //    private var shuffledList: List<SongModel> = emptyList()
//    private var playingList: ListIterator<SongModel> = listOf<SongModel>().listIterator()
//    private var playingList: List<SongModel> = emptyList()
    private lateinit var config: ConfigStore
    private lateinit var repeat: RepeatState
    private var shuffle = false
//    var song: SongModel? = null

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
        println("getCurrentSong ${exoPlayer?.currentPosition}")

        if (exoPlayer?.currentMediaItem == null) return null

        return playingList.first{MediaItem.fromUri(it.getMediaUri()) == exoPlayer?.currentMediaItem}
    }

    fun start(songs: List<SongModel>, index: Int) {
        playingList = songs
//        song = originalList[index]
        setRepeat()
        setShuffle()
        setExoPlayer()
        exoPlayer?.seekTo(index, 0)
        play()
        handler.post(runnable)
//        listener?.onChange()
    }

    fun play() {
        try {
            exoPlayer?.play()
//            listener?.onChange()
        } catch (e: IllegalStateException) {
            fix()
        }
    }

    fun pause() {
        exoPlayer?.pause()
//        listener?.onChange()
    }

    fun prev() {
        val isContinue = exoPlayer?.isPlaying

        if (getCurrentPosition() <= PREV_MS) {
            exoPlayer?.seekToPrevious()
        } else {
            exoPlayer?.seekTo(0)
        }

        if (isContinue == true) {
            play()
        }

//        listener?.onChange()
    }

    fun next() {
        val isContinue = exoPlayer?.isPlaying

        exoPlayer?.seekToNext()

        if (isContinue == true) {
            play()
        }

//        listener?.onChange()
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
//        listener?.onChange()
    }

    fun getShuffle(): Boolean {
        return shuffle
    }

    fun changeShuffle() {
        shuffle = !shuffle
        setShuffle()
        config.saveShuffle(shuffle)
//        listener?.onChange()
    }

    fun seekTo(ms: Long) {
        exoPlayer?.seekTo(ms)
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
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

    private fun shuffleOn() {
//        val tempList = originalList.toMutableList()
//        tempList.remove(this.song)
//        val shuffledList = tempList.shuffled().toMutableList()
//        this.song?.let { shuffledList.add(0, it) }
//        this.shuffledList = shuffledList
//        playingList = shuffledList.listIterator()
//        playingList = shuffledList
//        playingList.next()
    }

    private fun shuffleOff() {
//        playingList = originalList.listIterator(originalList.indexOf(this.song))
//        playingList = originalList
//        shuffledList = emptyList()
//        playingList.next()
    }

    private fun setExoPlayer() {
        destroy()

//        this.song = song
//        mediaPlayer = MediaPlayer.create(baseContext, song.getMediaUri())
//        mediaPlayer?.setOnCompletionListener(createCompletionListener())
        try {
            exoPlayer = ExoPlayer.Builder(baseContext).setLooper(Looper.getMainLooper()).build()
            val mediaItems = playingList.map {
                MediaItem.fromUri(it.getMediaUri())
            }
            exoPlayer?.setMediaItems(mediaItems)
            exoPlayer?.prepare()
            mediaSession = MediaSession.Builder(baseContext, exoPlayer!!).build()

            addListener()
        } catch (e: IllegalStateException) {
            println(e)
        }
    }

    private fun addListener() {
        exoPlayer?.addListener(object : Player.Listener {
//            override fun onEvents(player: Player, events: Player.Events) {
//
//            }
//
//            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//
//            }


            override fun onEvents(player: Player, events: Player.Events) {
                println("exoPlayer onEvents start")

                for (index in 0 until events.size()) {
                    println("exoPlayer onEvents $index:${index + 1}=${events[index]}")
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
                listener?.onChange()
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                println("exoPlayer onMediaMetadataChanged")
            }

            override  fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
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
                println("exoPlayer onIsPlayingChanged")
                listener?.onChange()
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
    private val runnable = object: Runnable {
        override fun run(){
            println("runnable!")
            handler.postDelayed(this, INTERVAL_MS) // intervalに設定したミリ秒後にコールバックを呼び出す
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}