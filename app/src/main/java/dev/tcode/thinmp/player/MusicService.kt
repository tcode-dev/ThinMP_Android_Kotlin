package dev.tcode.thinmp.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import dev.tcode.thinmp.model.SongModel

class MusicService : Service() {
    var binder: IBinder = MusicBinder()
    lateinit var song: SongModel
    private lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    fun initStart(song: SongModel) {
        this.song = song
        mediaPlayer = MediaPlayer.create(baseContext, song.getUri())
        mediaPlayer.start()
    }

    /**
     * bind
     */
    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    /**
     * interface
     */
    interface OnMusicServiceListener {
        /**
         * 曲変更
         */
        fun onChangeTrack(song: SongModel)

        /**
         * 再生開始
         */
        fun onStarted()

        /**
         * 再生終了
         */
        fun onFinished()

        /**
         * 強制終了
         */
        fun onForceFinished()

        /**
         * 画面を更新する必要がある
         */
        fun onScreenUpdate()
    }
}