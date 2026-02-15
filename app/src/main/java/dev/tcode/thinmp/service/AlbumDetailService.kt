package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.AlbumDetailModel
import dev.tcode.thinmp.repository.AlbumRepository
import dev.tcode.thinmp.repository.SongRepository

class AlbumDetailService(val context: Context) {
    fun findById(id: String): AlbumDetailModel? {
        val albumRepository = AlbumRepository(context)
        val songRepository = SongRepository(context)
        val album = albumRepository.findById(id)
        val songs = songRepository.findByAlbumId(id)
        val sortedSongs = songs.sortedBy { it.getTrackNumber() }

        return if (album != null) {
            AlbumDetailModel(id, album.name, album.artistName, album.getImageUri(), sortedSongs)
        } else {
            null
        }
    }
}