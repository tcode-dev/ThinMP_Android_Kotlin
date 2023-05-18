package dev.tcode.thinmp.player

//import android.media.MediaPlayer
//import android.media.MediaPlayer.OnCompletionListener

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel

interface MusicServiceListener {
    fun onChange() {}
}

class MusicService : Service() {
    private val PREV_MS = 3000
    private val binder = MusicBinder()
//    private var mediaPlayer: MediaPlayer? = null
    private var exoPlayer: ExoPlayer? = null
    private var listener: MusicServiceListener? = null
    private var originalList: List<SongModel> = emptyList()
    private var shuffledList: List<SongModel> = emptyList()
//    private var playingList: ListIterator<SongModel> = listOf<SongModel>().listIterator()
    private var playingList: List<SongModel> = emptyList()
    private lateinit var config: ConfigStore
    private lateinit var repeat: RepeatState
    private var shuffle = false
    var song: SongModel? = null

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

    fun start(songs: List<SongModel>, index: Int) {
        originalList = songs
        song = originalList[index]

        if (shuffle) {
            shuffleOn()
        } else {
            shuffleOff()
        }

        setExoPlayer(song!!)
        play()
        listener?.onChange()
    }

    fun play() {
        try {
            exoPlayer?.play()
            listener?.onChange()
        } catch (e: IllegalStateException) {
            fix()
        }
    }

    fun pause() {
        exoPlayer?.pause()
        listener?.onChange()
    }

    fun prev() {
//        val isContinue = exoPlayer?.isPlaying
//
//        if (getCurrentPosition() <= PREV_MS) {
//            if (!playingList.hasPrevious()) {
//                playingList = originalList.listIterator(originalList.count())
//            }
//
//            setExoPlayer(playingList.previous())
//        } else {
//            song?.let { setExoPlayer(it) }
//        }
//
//        if (isContinue == true) {
//            play()
//        }
//
//        listener?.onChange()
    }

    fun next() {
        val isContinue = exoPlayer?.isPlaying

//        if (!playingList.hasNext()) {
//            playingList = originalList.listIterator(0)
//        }

//        setExoPlayer(playingList.next())

        if (isContinue == true) {
            play()
        }

        listener?.onChange()
    }

    fun getRepeat(): RepeatState {
        return repeat
    }

    fun setRepeat() {
        repeat = when (repeat) {
            RepeatState.OFF -> RepeatState.ALL
            RepeatState.ONE -> RepeatState.OFF
            RepeatState.ALL -> RepeatState.ONE
        }

        config.saveRepeat(repeat)
        listener?.onChange()
    }

    fun getShuffle(): Boolean {
        return shuffle
    }

    fun setShuffle() {
        shuffle = !shuffle

        if (shuffle) {
            shuffleOn()
        } else {
            shuffleOff()
        }

        config.saveShuffle(shuffle)
        listener?.onChange()
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

    private fun shuffleOn() {
        val tempList = originalList.toMutableList()
        tempList.remove(this.song)
        val shuffledList = tempList.shuffled().toMutableList()
        this.song?.let { shuffledList.add(0, it) }
        this.shuffledList = shuffledList
//        playingList = shuffledList.listIterator()
        playingList = shuffledList
//        playingList.next()
    }

    private fun shuffleOff() {
//        playingList = originalList.listIterator(originalList.indexOf(this.song))
        playingList = originalList
        shuffledList = emptyList()
//        playingList.next()
    }

    private fun setExoPlayer(song: SongModel) {
        destroy()

        this.song = song
//        mediaPlayer = MediaPlayer.create(baseContext, song.getMediaUri())
//        mediaPlayer?.setOnCompletionListener(createCompletionListener())
        try {
            exoPlayer = ExoPlayer.Builder(baseContext).build()
            val mediaItems = playingList.map {
                MediaItem.fromUri(it.getMediaUri())
            }
            exoPlayer?.setMediaItems(mediaItems)
            exoPlayer?.prepare()
        } catch (e: IllegalStateException) {
            println(e)
        }
    }

    private fun addListener() {
        exoPlayer!!.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {

            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

            }
        })
    }
    private fun destroy() {
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.stop()
        }

        exoPlayer?.release()
        exoPlayer = null
        song = null
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

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}