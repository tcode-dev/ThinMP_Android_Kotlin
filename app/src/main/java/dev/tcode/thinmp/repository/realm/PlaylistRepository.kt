package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.realm.PlaylistRealmModel
import dev.tcode.thinmp.model.realm.PlaylistSongRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class PlaylistRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(PlaylistRealmModel::class))

        realm = Realm.open(config)
    }

    fun create(songId: SongId, name: String) {
        val playlist = PlaylistRealmModel()
        val song = PlaylistSongRealmModel()

        song.playlistId = playlist.id.toString()
        song.songId = songId.id

        playlist.name = name
        playlist.songs.addAll(listOf(song))

        realm.writeBlocking {
            copyToRealm(playlist)
        }
    }
}