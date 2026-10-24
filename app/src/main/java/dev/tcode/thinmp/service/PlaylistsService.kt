package dev.tcode.thinmp.service

import android.content.Context
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.PlaylistRepository

class PlaylistsService(val context: Context) {
    suspend fun findAll(): List<PlaylistModel> {
        val repository = PlaylistRepository()
        val playlists = repository.findAll()

        return playlists.map { PlaylistModel(PlaylistId(it.id), it.name) }
    }

    /** その曲が入っているプレイリスト。登録ポップアップで登録済みと表示するのに使う。 */
    suspend fun findRegisteredPlaylistIds(songId: SongId): Set<PlaylistId> {
        val repository = PlaylistRepository()

        return repository.findPlaylistIdsBySongId(songId).toSet()
    }
}