package dev.tcode.thinmp.repository.realm

import android.text.TextUtils
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

    fun add(playlistId: PlaylistId, songId: SongId) {
        realm.query<PlaylistRealmModel>("id == $0", playlistId.id)
            .first()
            .find()
            ?.also { playlist ->
                realm.writeBlocking {
                    val song = PlaylistSongRealmModel()
                    song.playlistId = playlistId.id
                    song.songId = songId.id
                    findLatest(playlist)?.songs?.add(song)
                }
            }
    }

    fun findAll(): List<PlaylistRealmModel> {
        return realm.query<PlaylistRealmModel>().find()
    }

    fun findById(playlistId: PlaylistId): PlaylistRealmModel? {
        return realm.query<PlaylistRealmModel>("id == $0", playlistId.id).first().find()
    }

    fun findByIds(playlistIds: List<PlaylistId>): List<PlaylistRealmModel> {
        val values = TextUtils.join(",", playlistIds.map {it.id})

        return realm.query<PlaylistRealmModel>("id in {$0}", values).find()
    }
}