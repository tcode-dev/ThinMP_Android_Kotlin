package dev.tcode.thinmp.repository

import androidx.room.withTransaction
import dev.tcode.thinmp.application.MainApplication
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.ShortcutId
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.constant.ItemType
import dev.tcode.thinmp.model.room.ShortcutEntity

class ShortcutRepository(
    private val db: AppDatabase = AppDatabase.getDatabase(MainApplication.appContext)
) {
    private val dao = db.shortcutDao()

    suspend fun exists(shortcutItemId: ShortcutItemId): Boolean {
        return when (shortcutItemId) {
            is ArtistId -> dao.exists(shortcutItemId.id, ItemType.ARTIST.ordinal)
            is AlbumId -> dao.exists(shortcutItemId.id, ItemType.ALBUM.ordinal)
            is PlaylistId -> dao.exists(shortcutItemId.id, ItemType.PLAYLIST.ordinal)
            else -> throw IllegalArgumentException("Unknown expression")
        }
    }

    suspend fun add(shortcutItemId: ShortcutItemId) {
        when (shortcutItemId) {
            is ArtistId -> add(shortcutItemId.id, ItemType.ARTIST)
            is AlbumId -> add(shortcutItemId.id, ItemType.ALBUM)
            is PlaylistId -> add(shortcutItemId.id, ItemType.PLAYLIST)
            else -> throw IllegalArgumentException("Unknown expression")
        }
    }

    suspend fun delete(shortcutItemId: ShortcutItemId) {
        when (shortcutItemId) {
            is ArtistId -> dao.deleteByItemIdAndType(shortcutItemId.id, ItemType.ARTIST.ordinal)
            is AlbumId -> dao.deleteByItemIdAndType(shortcutItemId.id, ItemType.ALBUM.ordinal)
            is PlaylistId -> dao.deleteByItemIdAndType(shortcutItemId.id, ItemType.PLAYLIST.ordinal)
            else -> throw IllegalArgumentException("Unknown expression")
        }
    }

    suspend fun findAll(): List<ShortcutEntity> {
        return dao.findAll()
    }

    suspend fun reorder(shortcutIds: List<ShortcutId>) = db.withTransaction {
        val shortcuts = findAll()
        val group = shortcuts.groupBy { shortcut -> shortcutIds.any { it.id == shortcut.id } }
        val deleteShortcuts = group[false] ?: emptyList()
        val sortedShortcuts = if (group[true] != null) {
            shortcutIds.mapNotNull { shortcutId -> group[true]?.first { it.id == shortcutId.id } }
        } else {
            emptyList()
        }

        if (deleteShortcuts.isNotEmpty()) {
            dao.deleteByIds(deleteShortcuts.map { it.id })
        }

        for ((index, shortcut) in sortedShortcuts.withIndex()) {
            dao.update(shortcut.copy(order = index + 1))
        }
    }

    suspend fun deleteByIds(ids: List<String>) {
        dao.deleteByIds(ids)
    }

    private suspend fun add(itemId: String, itemType: ItemType) {
        dao.insertAtEnd(itemId, itemType.ordinal)
    }
}
