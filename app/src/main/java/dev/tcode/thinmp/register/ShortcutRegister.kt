package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ShortcutId
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.repository.ShortcutRepository

interface ShortcutRegister {
    suspend fun exists(shortcutItemId: ShortcutItemId): Boolean {
        val repository = ShortcutRepository()

        return repository.exists(shortcutItemId)
    }

    suspend fun add(shortcutItemId: ShortcutItemId) {
        val repository = ShortcutRepository()

        repository.add(shortcutItemId)
    }

    suspend fun delete(shortcutItemId: ShortcutItemId) {
        val repository = ShortcutRepository()

        repository.delete(shortcutItemId)
    }

    suspend fun reorderShortcuts(shortcutIds: List<ShortcutId>) {
        val repository = ShortcutRepository()

        repository.reorder(shortcutIds)
    }
}