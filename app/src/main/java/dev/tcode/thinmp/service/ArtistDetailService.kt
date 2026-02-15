package dev.tcode.thinmp.service

import android.content.Context
import android.net.Uri
import dev.tcode.thinmp.model.media.ArtistDetailModel
import dev.tcode.thinmp.repository.AlbumRepository
import dev.tcode.thinmp.repository.ArtistRepository
import dev.tcode.thinmp.repository.SongRepository

class ArtistDetailService(val context: Context) {
    fun findById(id: String): ArtistDetailModel? {
        val artistRepository = ArtistRepository(context)
        val albumRepository = AlbumRepository(context)
        val songRepository = SongRepository(context)
        val artist = artistRepository.findById(id)
        val albums = albumRepository.findByArtistId(id)
        val songs = songRepository.findByArtistId(id)
        val secondaryText = "${albums.count()} albums, ${songs.count()} songs"
        val imageUri = if (albums.isNotEmpty()) albums.first().getImageUri() else Uri.EMPTY

        return if (artist != null) {
            ArtistDetailModel(id, artist.name, secondaryText, imageUri, albums, songs)
        } else {
            null
        }
    }
}