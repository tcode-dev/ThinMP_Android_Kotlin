package dev.tcode.thinmp.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Binder
import android.os.IBinder
import dev.tcode.thinmp.config.ConfigStore
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.model.media.SongModel

interface MusicServiceListener {
    fun onChange() {}
}

class MusicService : Service() {
    private val PREV_MS = 3000
    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var listener: MusicServiceListener? = null
    private var originalList: List<SongModel> = emptyList()
    private var shuffledList: List<SongModel> = emptyList()
    private var playingList: ListIterator<SongModel> = listOf<SongModel>().listIterator()
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

        setMediaPlayer(song!!)
        play()
    }

    fun play() {
        mediaPlayer?.start()
        listener?.onChange()
    }

    fun pause() {
        mediaPlayer?.pause()
        listener?.onChange()
    }

    fun prev() {
        val isContinue = mediaPlayer?.isPlaying

        if (getCurrentPosition() <= PREV_MS) {
            if (!playingList.hasPrevious()) {
                playingList = originalList.listIterator(originalList.count())
            }

            setMediaPlayer(playingList.previous())
        } else {
            song?.let { setMediaPlayer(it) }
        }

        if (isContinue == true) {
            mediaPlayer?.start()
        }

        listener?.onChange()
    }

    fun next() {
        val isContinue = mediaPlayer?.isPlaying

        if (!playingList.hasNext()) {
            playingList = originalList.listIterator(0)
        }

        setMediaPlayer(playingList.next())

        if (isContinue == true) {
            mediaPlayer?.start()
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

    fun seekTo(msec: Int) {
        mediaPlayer?.seekTo(msec)
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    private fun shuffleOn() {
        val tempList = originalList.toMutableList()
        tempList.remove(this.song)
        val shuffledList = tempList.shuffled().toMutableList()
        this.song?.let { shuffledList.add(0, it) }
        this.shuffledList = shuffledList
        playingList = shuffledList.listIterator()
        playingList.next()
    }

    private fun shuffleOff() {
        playingList = originalList.listIterator(originalList.indexOf(this.song))
        shuffledList = emptyList()
        playingList.next()
    }

    private fun setMediaPlayer(song: SongModel) {
        destroy()

        this.song = song
        mediaPlayer = MediaPlayer.create(baseContext, song.getMediaUri())
        mediaPlayer?.setOnCompletionListener(createCompletionListener())
    }

    private fun destroy() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }

        mediaPlayer?.release()
        mediaPlayer = null
        song = null
    }

    private fun createCompletionListener(): OnCompletionListener {
        return OnCompletionListener {
            if (repeat == RepeatState.ONE) {
                song?.let { setMediaPlayer(it) }
                mediaPlayer?.start()
            } else {
                if (playingList.hasNext()) {
                    setMediaPlayer(playingList.next())
                    mediaPlayer?.start()
                } else {
                    playingList = if (shuffle) {
                        shuffledList.listIterator(0)
                    } else {
                        originalList.listIterator(0)
                    }
                    setMediaPlayer(playingList.next())
                    if (repeat == RepeatState.ALL) {
                        mediaPlayer?.start()
                    }
                }
            }

            listener?.onChange()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}