package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.realm.PlaylistRealmModel
import dev.tcode.thinmp.model.realm.PlaylistSongRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import org.mongodb.kbson.BsonObjectId

class PlaylistRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(PlaylistRealmModel::class, PlaylistSongRealmModel::class))

        realm = Realm.open(config)
    }

    fun create(songId: SongId, name: String) {
        val playlist = PlaylistRealmModel()
        val song = PlaylistSongRealmModel()

        song.playlistId = playlist.id.toString()
        song.songId = songId.id

        playlist.name = name
        playlist.songs.add(song)

        realm.writeBlocking {
            copyToRealm(playlist)
        }
    }

    fun add(playlistId: BsonObjectId, songId: SongId) {

    }

    fun findAll(): List<PlaylistRealmModel> {
        return realm.query<PlaylistRealmModel>().find()
    }
}