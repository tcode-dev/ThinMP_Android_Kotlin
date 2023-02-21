package dev.tcode.thinmp.register

import dev.tcode.thinmp.model.media.valueObject.ItemId
import dev.tcode.thinmp.repository.realm.ItemType
import dev.tcode.thinmp.repository.realm.ShortcutRepository

interface ShortcutRegister {
    fun addShortcut(itemId: ItemId, itemType: ItemType) {
        val repository = ShortcutRepository()

        repository.add(itemId, itemType)
    }
}