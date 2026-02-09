package dev.tcode.thinmp.service

import android.content.Context
import android.net.Uri
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.PlaylistDetailModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.media.SongRepository
import dev.tcode.thinmp.repository.room.PlaylistRepository

class PlaylistDetailService(val context: Context, private val playlistRepository: PlaylistRepository = PlaylistRepository()) {
    fun findById(playlistId: PlaylistId): PlaylistDetailModel? {
        val resources = context.resources
        val playlist = playlistRepository.findById(playlistId) ?: return null
        val playlistSongs = playlistRepository.findSongsByPlaylistId(playlistId)
        val songIds = playlistSongs.map { SongId(it.songId) }
        val songRepository = SongRepository(context)
        val songs = songRepository.findByIds(songIds)
        val sortedSongs = songIds.mapNotNull { id ->
            songs.find { it.songId == id }
        }
        val imageUri = if (sortedSongs.isNotEmpty()) sortedSongs.first().getImageUri() else Uri.EMPTY

        if (!validation(songIds, songs)) {
            fix(playlistId, songIds, songs)

            return findById(playlistId)
        }

        return PlaylistDetailModel(playlistId, playlist.name, resources.getString(R.string.playlist), imageUri, sortedSongs)
    }

    private fun validation(songIds: List<SongId>, songs: List<SongModel>): Boolean {
        return songIds.count() == songs.count()
    }

    private fun fix(playlistId: PlaylistId, songIds: List<SongId>, songs: List<SongModel>) {
        val deleteIds = songIds.filter { id ->
            songs.none { it.songId == id }
        }

        playlistRepository.delete(playlistId, deleteIds)
    }
}
