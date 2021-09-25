package dev.tcode.thinmp.viewModel

import android.content.Context
import dev.tcode.thinmp.model.SongModel
import dev.tcode.thinmp.repository.media.SongRepository

class SongsViewModel {
    lateinit var songs: List<SongModel>

    fun load(context: Context) {
        val repository = SongRepository(context)

        songs = repository.findAll()
    }
}