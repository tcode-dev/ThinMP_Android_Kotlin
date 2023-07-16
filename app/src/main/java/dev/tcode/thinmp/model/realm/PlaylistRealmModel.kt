package dev.tcode.thinmp.model.realm

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class PlaylistRealmModel: RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var songs: RealmList<PlaylistSongRealmModel> = realmListOf()
    var order: Int = 0
}