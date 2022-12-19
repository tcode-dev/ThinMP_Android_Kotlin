package dev.tcode.thinmp.player

import android.content.Context
import android.media.MediaPlayer
import dev.tcode.thinmp.model.media.SongModel

class MusicPlayer {
    fun start(context: Context, song: SongModel) {
        val mediaPlayer = MediaPlayer.create(context, song.getUri())

        mediaPlayer?.start()
    }
}
