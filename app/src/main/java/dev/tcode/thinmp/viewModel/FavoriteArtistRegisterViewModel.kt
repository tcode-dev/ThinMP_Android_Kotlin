package dev.tcode.thinmp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.register.FavoriteArtistRegister
import kotlinx.coroutines.launch

class FavoriteArtistRegisterViewModel : ViewModel(), FavoriteArtistRegister {
    suspend fun isFavorite(artistId: ArtistId): Boolean {
        return existsFavoriteArtist(artistId)
    }

    /**
     * Runs in viewModelScope rather than the caller's scope: the menu closes as soon as this is
     * invoked, so a composition-scoped coroutine would be cancelled before the write lands.
     *
     * The current state is decided inside the transaction rather than passed in from the menu
     * label, so a stale label cannot turn an add into a second add.
     */
    fun toggle(artistId: ArtistId) {
        viewModelScope.launch {
            toggleFavoriteArtist(artistId)
        }
    }
}
