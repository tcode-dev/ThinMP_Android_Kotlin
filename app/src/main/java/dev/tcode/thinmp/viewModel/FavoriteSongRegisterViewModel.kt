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
     * 呼ばれるとすぐにメニューが閉じるので、呼び出し元のスコープではなくviewModelScopeで書き込む。
     * compositionのスコープだと、書き込みの前にキャンセルされてしまう。
     *
     * 現在の状態はメニューの表示から受け取らず、トランザクションの中で確かめる。
     * 表示が古くても二重に追加しないようにするため。
     */
    fun toggle(songId: SongId) {
        viewModelScope.launch {
            toggleFavoriteSong(songId)
        }
    }
}
