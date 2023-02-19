package dev.tcode.thinmp.repository.realm

import dev.tcode.thinmp.model.media.valueObject.ItemId
import dev.tcode.thinmp.model.realm.ShortcutRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

enum class ItemType {
    ARTIST, ALBUM, PLAYLIST
}

class ShortcutRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(ShortcutRealmModel::class))

        realm = Realm.open(config)
    }

    fun add(_itemId: ItemId, itemType: ItemType) {
        realm.writeBlocking {
            copyToRealm(ShortcutRealmModel().apply {
                itemId = _itemId.id
                type = itemType.ordinal
            })
        }
    }
}