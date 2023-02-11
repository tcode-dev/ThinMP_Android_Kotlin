package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.PlaylistDetailModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.repository.realm.PlaylistRepository

class PlaylistDetailService(val context: Context) {
    fun findById(playlistId: PlaylistId): PlaylistDetailModel? {
        val playlistRepository = PlaylistRepository()
        val playlist = playlistRepository.findById(playlistId) ?: return null
        val songIds = playlist.songs.map{ SongId(it.songId) }
        val songRepository = SongRepository(context)
        val songs = songRepository.findByIds(songIds)

        return PlaylistDetailModel(playlistId, playlist.name, "playlist", songs.first().getImageUri(), songs)
    }
}