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
     * ショートカットの切り替えはこれだけにし、追加と削除は公開しない。
     * exists()で確かめてから書き込むと間に中断点ができ、ダブルタップで2回挿入されてしまう。
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