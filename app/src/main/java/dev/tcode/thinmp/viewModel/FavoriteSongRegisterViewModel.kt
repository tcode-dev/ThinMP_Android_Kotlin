package dev.tcode.thinmp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.register.FavoriteSongRegister
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteSongRegisterViewModel @Inject constructor() : ViewModel(), FavoriteSongRegister {
    suspend fun isFavorite(songId: SongId): Boolean {
        return existsFavoriteSong(songId)
    }

    /**
     * 呼ばれるとすぐにメニューが閉じるので、呼び出し元のスコープではなくviewModelScopeで書き込む。
     * compositionのスコープだと、書き込みの前にキャンセルされてしまう。
     *
     * 現在の状態はメニューの表示から受け取らず、トランザクションの中で確かめる。
     * 表示が古くても二重に追加しないようにするため。
     *
     * [onToggled]は書き込みが終わってから呼ぶ。
     * 一覧の読み直しが書き込みより先に終わると、外した行が残ってしまうからだ。
     */
    fun toggle(songId: SongId, onToggled: () -> Unit = {}) {
        viewModelScope.launch {
            toggleFavoriteSong(songId)
            onToggled()
        }
    }
}
