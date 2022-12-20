package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.repository.media.AlbumRepository

class AlbumsService(val context: Context) {
    fun findAll(): List<AlbumModel> {
        val repository = AlbumRepository(context)

        return repository.findAll()
    }
}
