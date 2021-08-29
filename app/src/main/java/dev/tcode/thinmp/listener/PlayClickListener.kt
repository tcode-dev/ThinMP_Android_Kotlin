package dev.tcode.thinmp.listener

import android.media.MediaPlayer
import android.view.View
import dev.tcode.thinmp.model.SongModel

class PlayClickListener(private val song: SongModel) : View.OnClickListener {
    override fun onClick(view: View) {
        val mediaPlayer = MediaPlayer.create(view.context, song.getUri())

        mediaPlayer.start()
    }
}