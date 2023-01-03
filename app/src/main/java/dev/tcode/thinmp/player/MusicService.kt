package dev.tcode.thinmp.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Binder
import android.os.IBinder
import dev.tcode.thinmp.model.media.SongModel

interface MusicServiceListener {
    fun onChange() {}
}

class MusicService : Service() {
    private val binder = MusicBinder()
    private var mediaPlayer : MediaPlayer? = null
    private var listener: MusicServiceListener? = null
    private var songs: ListIterator<SongModel> = listOf<SongModel>().listIterator()
    var song: SongModel? = null

    fun setListener(listener: MusicServiceListener) {
        this.listener = listener
    }

    fun start(songs: List<SongModel>, index: Int) {
        this.songs = songs.listIterator(index)

        setMediaPlayer(this.songs.next())
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

    fun next() {
        val isContinue = mediaPlayer?.isPlaying

        setMediaPlayer(songs.next())

        if (isContinue == true) {
            mediaPlayer?.start()
        }

        listener?.onChange()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
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
            if (songs.hasNext()) {
                setMediaPlayer(songs.next())

                mediaPlayer?.start()
                listener?.onChange()
            } else {
                destroy()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}
