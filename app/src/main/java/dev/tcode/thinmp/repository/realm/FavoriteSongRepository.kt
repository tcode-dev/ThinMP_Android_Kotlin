package dev.tcode.thinmp.repository.realm

import android.text.TextUtils
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.realm.FavoriteSongRealmModel
import dev.tcode.thinmp.model.realm.ShortcutRealmModel
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

    fun delete(songId: SongId) {
        realm.writeBlocking {
            val song = find(songId).first()

            findLatest(song)?.let { delete(it) }
        }
    }

    fun update(ids: List<SongId>) {
        realm.writeBlocking {
            val values = TextUtils.join(", ", ids.map { "'${it.id}'" })
            val models = realm.query<FavoriteSongRealmModel>("songId in { $values }").find()

            models.forEach { shortcut ->
                findLatest(shortcut)?.let { delete(it) }
            }
        }
    }

    private fun find(songId: SongId): RealmResults<FavoriteSongRealmModel> {
        return realm.query<FavoriteSongRealmModel>("songId == $0", songId.id).find()
    }
}