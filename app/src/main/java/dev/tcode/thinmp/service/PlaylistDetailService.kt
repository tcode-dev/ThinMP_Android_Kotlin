package dev.tcode.thinmp.service

import android.content.Context
import android.net.Uri
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.PlaylistDetailModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.SongRepository
import dev.tcode.thinmp.repository.PlaylistRepository

class PlaylistDetailService(val context: Context, private val playlistRepository: PlaylistRepository = PlaylistRepository()) {
    suspend fun findById(playlistId: PlaylistId): PlaylistDetailModel? {
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

        removeMissing(playlistId, songIds, songs)

        return PlaylistDetailModel(playlistId, playlist.name, resources.getString(R.string.playlist), imageUri, sortedSongs)
    }

    /**
     * Drops entries whose file has left MediaStore. This used to compare songIds.count() with
     * songs.count() and re-enter findById() on a mismatch, which never terminated for a playlist
     * holding the same song twice: SQL IN collapses the duplicate so songs is one shorter, yet
     * the id is present and nothing gets deleted. Adding a song to a playlist twice is an
     * ordinary thing to do, so that hung the detail screen for good.
     *
     * sortedSongs maps per id rather than per MediaStore row, so a song listed twice still shows
     * up twice, which is what the playlist says.
     */
    private suspend fun removeMissing(playlistId: PlaylistId, songIds: List<SongId>, songs: List<SongModel>) {
        val deleteIds = songIds.filter { id ->
            songs.none { it.songId == id }
        }

        if (deleteIds.isEmpty()) return

        playlistRepository.delete(playlistId, deleteIds)
    }
}
