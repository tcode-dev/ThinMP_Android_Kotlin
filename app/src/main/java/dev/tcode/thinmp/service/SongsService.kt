package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.repository.media.SongRepository

class SongsService(val context: Context) {
    fun findAll(): List<SongModel> {
        val repository = SongRepository(context)

        return repository.findAll()
    }
}
