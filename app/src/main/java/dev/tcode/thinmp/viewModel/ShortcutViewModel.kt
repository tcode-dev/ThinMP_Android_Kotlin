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
     *
     * The current state is decided inside the transaction rather than passed in from the menu
     * label, so a stale label cannot turn an add into a second add.
     */
    fun toggle(shortcutItemId: ShortcutItemId) {
        viewModelScope.launch {
            toggleShortcut(shortcutItemId)
        }
    }
}
