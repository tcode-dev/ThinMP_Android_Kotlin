package dev.tcode.thinmp.repository.realm

import android.text.TextUtils
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.realm.FavoriteSongRealmModel
import dev.tcode.thinmp.model.realm.PlaylistSongRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults

class FavoriteSongRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(FavoriteSongRealmModel::class))

        realm = Realm.open(config)
    }

    fun exists(songId: SongId): Boolean {
        return find(songId).isNotEmpty()
    }

    fun findAll(): List<SongId> {
        return realm.query<FavoriteSongRealmModel>().find().map {
            SongId(it.songId)
        }
    }

    fun add(_songId: SongId) {
        realm.writeBlocking {
            copyToRealm(FavoriteSongRealmModel().apply {
                songId = _songId.id
            })
        }
    }

    fun update(songIds: List<SongId>) {
        realm.writeBlocking {
            delete(FavoriteSongRealmModel::class)

            songIds.forEach {
                copyToRealm(FavoriteSongRealmModel().apply {
                    songId = it.id
                })
            }
        }
    }

    fun delete(songId: SongId) {
        realm.writeBlocking {
            val song = find(songId).first()

            findLatest(song)?.let { delete(it) }
        }
    }

    fun deleteByIds(songIds: List<SongId>) {
        realm.writeBlocking {
            val values = TextUtils.join(", ", songIds.map { "'${it.id}'" })
            val songs = realm.query<FavoriteSongRealmModel>("songId in { $values }").find()

            songs.forEach { song ->
                findLatest(song)?.let { delete(it) }
            }
        }
    }

    private fun find(songId: SongId): RealmResults<FavoriteSongRealmModel> {
        return realm.query<FavoriteSongRealmModel>("songId == $0", songId.id).find()
    }
}