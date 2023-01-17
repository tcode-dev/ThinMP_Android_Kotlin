package dev.tcode.thinmp.model.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId

class FavoriteSongRealmModel: RealmObject {
    @PrimaryKey
    var id: BsonObjectId = BsonObjectId()
    var songId: String = ""
}