package dev.tcode.thinmp.repository.realm

import android.text.TextUtils
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.model.realm.ShortcutRealmModel
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults

enum class ItemType {
    ARTIST, ALBUM, PLAYLIST
}

class ShortcutRepository {
    private val realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(ShortcutRealmModel::class))

        realm = Realm.open(config)
    }

    fun exists(shortcutItemId: ShortcutItemId): Boolean {
        return when (shortcutItemId) {
            is ArtistId -> exists(shortcutItemId.id, ItemType.ARTIST)
            is AlbumId -> exists(shortcutItemId.id, ItemType.ALBUM)
            is PlaylistId -> exists(shortcutItemId.id, ItemType.PLAYLIST)
            else -> throw IllegalArgumentException("Unknown expression")
        }
    }

    fun add(shortcutItemId: ShortcutItemId) {
        when (shortcutItemId) {
            is ArtistId -> add(shortcutItemId.id, ItemType.ARTIST)
            is AlbumId -> add(shortcutItemId.id, ItemType.ALBUM)
            is PlaylistId -> add(shortcutItemId.id, ItemType.PLAYLIST)
            else -> throw IllegalArgumentException("Unknown expression")
        }
    }

    fun delete(shortcutItemId: ShortcutItemId) {
        when (shortcutItemId) {
            is ArtistId -> delete(shortcutItemId.id, ItemType.ARTIST)
            is AlbumId -> delete(shortcutItemId.id, ItemType.ALBUM)
            is PlaylistId -> delete(shortcutItemId.id, ItemType.PLAYLIST)
            else -> throw IllegalArgumentException("Unknown expression")
        }
    }

    fun findAll(): List<ShortcutRealmModel> {
        return realm.query<ShortcutRealmModel>().find().sortedByDescending(ShortcutRealmModel::order)
    }

    fun update(ids: List<String>) {
        realm.writeBlocking {
            val values = TextUtils.join(", ", ids.map { "'${it}'" })
            val shortcuts = realm.query<ShortcutRealmModel>("id in { $values }").find()

            // 一度にまとめて削除できない
            // @see https://github.com/realm/realm-kotlin#delete
            shortcuts.forEach { shortcut ->
                findLatest(shortcut)?.let { delete(it) }
            }
        }
    }

    private fun exists(itemId: String, itemType: ItemType): Boolean {
        return find(itemId, itemType).isNotEmpty()
    }

    private fun find(itemId: String, itemType: ItemType): RealmResults<ShortcutRealmModel> {
        return realm.query<ShortcutRealmModel>("itemId == $0 AND type = $1", itemId, itemType.ordinal).find()
    }

    private fun add(_itemId: String, itemType: ItemType) {
        realm.writeBlocking {
            copyToRealm(ShortcutRealmModel().apply {
                itemId = _itemId
                type = itemType.ordinal
                order = increment()
            })
        }
    }

    private fun delete(itemId: String, itemType: ItemType) {
        realm.writeBlocking {
            val shortcut = find(itemId, itemType).first()

            findLatest(shortcut)?.let { delete(it) }
        }
    }

    private fun increment(): Int {
        val result = realm.query<ShortcutRealmModel>().find()

        if (result.isEmpty()) return 1

        return result.maxOf(ShortcutRealmModel::order) + 1
    }
}