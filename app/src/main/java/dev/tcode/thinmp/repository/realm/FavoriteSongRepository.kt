package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.realm.FavoriteSongRealmModel
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

    fun exists(songId: String): Boolean {
        val song: RealmResults<FavoriteSongRealmModel> = realm.query<FavoriteSongRealmModel>("songId == $0", songId).find()

        return song.isNotEmpty()
    }

    fun add(_songId: String) {
        realm.writeBlocking {
            copyToRealm(FavoriteSongRealmModel().apply {
                songId = _songId
            })
        }
    }
}