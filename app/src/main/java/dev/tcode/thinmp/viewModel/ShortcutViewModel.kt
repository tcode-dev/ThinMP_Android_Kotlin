package dev.tcode.thinmp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.valueObject.ShortcutItemId
import dev.tcode.thinmp.register.ShortcutRegister
import kotlinx.coroutines.launch

class ShortcutViewModel : ViewModel(), ShortcutRegister {
    suspend fun isShortcut(shortcutItemId: ShortcutItemId): Boolean {
        return exists(shortcutItemId)
    }

    /**
     * Runs in viewModelScope rather than the caller's scope: the menu closes as soon as this is
     * invoked, so a composition-scoped coroutine would be cancelled before the write lands.
     */
    fun toggle(shortcutItemId: ShortcutItemId, isShortcut: Boolean) {
        viewModelScope.launch {
            if (isShortcut) {
                delete(shortcutItemId)
            } else {
                add(shortcutItemId)
            }
        }
    }
}
