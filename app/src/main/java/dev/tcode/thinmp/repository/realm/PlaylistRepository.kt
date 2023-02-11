package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.realm.PlaylistRealmModel
import dev.tcode.thinmp.model.realm.PlaylistSongRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query

class PlaylistRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(PlaylistRealmModel::class, PlaylistSongRealmModel::class))

        realm = Realm.open(config)
    }

    fun create(songId: SongId, name: String) {
        val playlist = PlaylistRealmModel()
        val song = PlaylistSongRealmModel()

        song.playlistId = playlist.id
        song.songId = songId.id

        playlist.name = name
        playlist.songs.add(song)

        realm.writeBlocking {
            copyToRealm(playlist)
        }
    }

    fun add(id: PlaylistId, songId: SongId) {
//        val playlist = realm.query<PlaylistRealmModel>("id == $0", id).first().find()
//        val song = PlaylistSongRealmModel()
//
//        if (playlist == null) {
//            return
//        }
//
//        song.playlistId = playlistId
//        song.songId = songId.id
//
//        playlist.songs.add(song)
//
//        realm.writeBlocking {
//            copyToRealm(playlist)
//        }
    }

    fun findAll(): List<PlaylistRealmModel> {
        return realm.query<PlaylistRealmModel>().find()
    }

    fun findById(playlistId: PlaylistId): PlaylistRealmModel? {
        return realm.query<PlaylistRealmModel>("id == $0", playlistId.id).first().find()
    }
}