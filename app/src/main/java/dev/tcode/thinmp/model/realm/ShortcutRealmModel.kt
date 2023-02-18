package dev.tcode.thinmp.model.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.*

class ShortcutRealmModel: RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var itemId: String = ""
    var type: Int = 0
    var order: Int = 0
}