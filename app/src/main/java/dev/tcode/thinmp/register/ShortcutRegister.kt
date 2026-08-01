package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ShortcutId
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.repository.ShortcutRepository

interface ShortcutRegister {
    suspend fun exists(shortcutItemId: ShortcutItemId): Boolean {
        val repository = ShortcutRepository()

        return repository.exists(shortcutItemId)
    }

    /**
     * The only way to flip one item's shortcut state. Deliberately not exposed as separate add
     * and delete: reading exists() and then writing leaves a suspension point between the two,
     * so a double tap inserts the item twice.
     */
    suspend fun toggleShortcut(shortcutItemId: ShortcutItemId) {
        val repository = ShortcutRepository()

        repository.toggle(shortcutItemId)
    }

    suspend fun reorderShortcuts(shortcutIds: List<ShortcutId>) {
        val repository = ShortcutRepository()

        repository.reorder(shortcutIds)
    }
}