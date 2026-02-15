package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ShortcutId
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.repository.ShortcutRepository

interface ShortcutRegister {
    fun exists(shortcutItemId: ShortcutItemId): Boolean {
        val repository = ShortcutRepository()

        return repository.exists(shortcutItemId)
    }

    fun add(shortcutItemId: ShortcutItemId) {
        val repository = ShortcutRepository()

        repository.add(shortcutItemId)
    }

    fun delete(shortcutItemId: ShortcutItemId) {
        val repository = ShortcutRepository()

        repository.delete(shortcutItemId)
    }

    fun update(shortcutIds: List<ShortcutId>) {
        val repository = ShortcutRepository()

        repository.update(shortcutIds)
    }
}