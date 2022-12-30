package dev.tcode.thinmp.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Binder
import android.os.IBinder
import dev.tcode.thinmp.model.media.SongModel

class MusicService : Service() {
    private val binder = MusicBinder()
    private var mediaPlayer : MediaPlayer? = null
    private var songs: ListIterator<SongModel> = listOf<SongModel>().listIterator()
    var song: SongModel? = null

    fun start(songs: List<SongModel>, index: Int) {
        this.songs = songs.listIterator(index)

        setMediaPlayer(this.songs.next())

        mediaPlayer?.start()
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
        return label@ OnCompletionListener { mp: MediaPlayer? ->
            if (songs.hasNext()) {
                setMediaPlayer(songs.next())

                mediaPlayer?.start()
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
