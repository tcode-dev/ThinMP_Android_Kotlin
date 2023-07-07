package dev.tcode.thinmp.repository.realm

import android.text.TextUtils
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.model.realm.FavoriteArtistRealmModel
import dev.tcode.thinmp.model.realm.FavoriteSongRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults

class FavoriteArtistRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(FavoriteArtistRealmModel::class))

        realm = Realm.open(config)
    }

    fun exists(artistId: ArtistId): Boolean {
        return find(artistId).isNotEmpty()
    }

    fun findAll(): List<ArtistId> {
        return realm.query<FavoriteArtistRealmModel>().find().map {
            ArtistId(it.artistId)
        }
    }

    fun add(_artistId: ArtistId) {
        realm.writeBlocking {
            copyToRealm(FavoriteArtistRealmModel().apply {
                artistId = _artistId.id
            })
        }
    }

    fun update(artistIds: List<ArtistId>) {
        realm.writeBlocking {
            delete(FavoriteArtistRealmModel::class)

            artistIds.forEach {
                copyToRealm(FavoriteArtistRealmModel().apply {
                    artistId = it.id
                })
            }
        }
    }

    fun delete(artistId: ArtistId) {
        realm.writeBlocking {
            val artist = find(artistId).first()

            findLatest(artist)?.let { delete(it) }
        }
    }

    fun deleteByIds(artistIds: List<ArtistId>) {
        realm.writeBlocking {
            val values = TextUtils.join(", ", artistIds.map { "'${it.id}'" })
            val models = realm.query<FavoriteArtistRealmModel>("artistId in { $values }").find()

            models.forEach { model ->
                findLatest(model)?.let { delete(it) }
            }
        }
    }

    private fun find(artistId: ArtistId): RealmResults<FavoriteArtistRealmModel> {
        return realm.query<FavoriteArtistRealmModel>("artistId == $0", artistId.id).find()
    }
}