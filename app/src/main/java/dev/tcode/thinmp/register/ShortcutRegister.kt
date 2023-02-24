package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.repository.realm.ShortcutRepository

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
}