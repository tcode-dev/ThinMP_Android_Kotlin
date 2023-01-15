package dev.tcode.thinmp.model.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

class FavoriteSongRealmModel: RealmObject {
    val ID = "id"
    val SONG_ID = "songId"

    @PrimaryKey
    var id: BsonObjectId = ObjectId()
    var songId: String = ""

    fun set(songId: String) {
        this.songId = songId
    }
}