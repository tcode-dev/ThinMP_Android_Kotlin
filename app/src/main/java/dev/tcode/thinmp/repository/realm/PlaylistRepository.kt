package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.media.SongModel
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

    fun add(_name: String, _songs: List<SongModel>) {
        val song = PlaylistSongRealmModel().apply {
            playlistId = ""
            songId = ""
        }
        val playlist = PlaylistRealmModel().apply {
            name = _name
            order = 1
            songs.addAll(listOf(song))
        }

        realm.writeBlocking {
            copyToRealm(playlist)
        }
    }
}