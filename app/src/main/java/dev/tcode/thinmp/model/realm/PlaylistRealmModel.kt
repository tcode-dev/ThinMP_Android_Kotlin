package dev.tcode.thinmp.model.realm

import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId

class PlaylistRealmModel: RealmObject {
    @PrimaryKey
    var id: BsonObjectId = BsonObjectId()
    var name: String? = null
    var order = 0
    lateinit var songs: RealmList<PlaylistSongRealmModel>
}