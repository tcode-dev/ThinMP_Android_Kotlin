package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.realm.FavoriteSongRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class FavoriteSongRegister {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(FavoriteSongRealmModel::class))
        realm = Realm.open(config)
    }

    fun add(_songId: String) {
        realm.writeBlocking {
            copyToRealm(FavoriteSongRealmModel().apply {
                songId = _songId
            })
        }
    }
}