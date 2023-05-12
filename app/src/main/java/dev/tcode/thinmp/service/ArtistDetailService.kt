package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.ArtistDetailModel
import dev.tcode.thinmp.repository.media.AlbumRepository
import dev.tcode.thinmp.repository.media.ArtistRepository
import dev.tcode.thinmp.repository.media.SongRepository

class ArtistDetailService(val context: Context) {
    fun findById(id: String): ArtistDetailModel? {
        val artistRepository = ArtistRepository(context)
        val albumRepository = AlbumRepository(context)
        val songRepository = SongRepository(context)
        val artist = artistRepository.findById(id)
        val albums = albumRepository.findByArtistId(id)
        val sortedAlbums = albums.sortedBy { it.name }
        val songs = songRepository.findByArtistId(id)
        val sortedSongs = songs.sortedWith(compareBy({ it.albumName }, { it.getTrackNumber() }))
        val secondaryText = "${albums.count()} albums, ${songs.count()} songs"
        val imageUri = if (albums.isNotEmpty()) {
            albums.first().getImageUri()
        } else {
            songs.first().getImageUri()
        }

        return if (artist != null) {
            ArtistDetailModel(id, artist.name, secondaryText, imageUri, sortedAlbums, sortedSongs)
        } else {
            null
        }
    }
}