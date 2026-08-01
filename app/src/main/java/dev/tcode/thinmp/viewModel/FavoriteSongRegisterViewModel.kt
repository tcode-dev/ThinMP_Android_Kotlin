package dev.tcode.thinmp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.register.FavoriteSongRegister
import kotlinx.coroutines.launch

class FavoriteSongRegisterViewModel : ViewModel(), FavoriteSongRegister {
    suspend fun isFavorite(songId: SongId): Boolean {
        return existsFavoriteSong(songId)
    }

    /**
     * Runs in viewModelScope rather than the caller's scope: the menu closes as soon as this is
     * invoked, so a composition-scoped coroutine would be cancelled before the write lands.
     */
    fun toggle(songId: SongId, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                deleteFavoriteSong(songId)
            } else {
                addFavoriteSong(songId)
            }
        }
    }
}
