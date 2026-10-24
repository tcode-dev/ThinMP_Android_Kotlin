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
     * 呼ばれるとすぐにメニューが閉じるので、呼び出し元のスコープではなくviewModelScopeで書き込む。
     * compositionのスコープだと、書き込みの前にキャンセルされてしまう。
     *
     * 現在の状態はメニューの表示から受け取らず、トランザクションの中で確かめる。
     * 表示が古くても二重に追加しないようにするため。
     */
    fun toggle(artistId: ArtistId) {
        viewModelScope.launch {
            toggleFavoriteArtist(artistId)
        }
    }
}
