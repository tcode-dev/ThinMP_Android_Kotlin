package dev.tcode.thinmp.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.player.PlaybackController
import dev.tcode.thinmp.player.PlaybackState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MiniPlayerUiState(
    val primaryText: String = "", val imageUri: Uri = Uri.EMPTY, val isVisible: Boolean = false, val isPlaying: Boolean = false
)

/**
 * PlaybackController.stateを現在値から引き継ぐので、
 * 開いたばかりの画面でも最初のフレームから再生中の曲を表示できる。
 * キューに曲があるときだけ表示するので、retry()でキューが空になれば消える。
 */
class MiniPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val playbackController = PlaybackController.from(application)

    val uiState: StateFlow<MiniPlayerUiState> =
        playbackController.state.map { toUiState(it) }.stateIn(viewModelScope, SharingStarted.Eagerly, toUiState(playbackController.state.value))

    fun toggle() {
        if (playbackController.state.value.isPlaying) {
            playbackController.pause()
        } else {
            playbackController.play()
        }
    }

    fun next() {
        playbackController.next()
    }

    private fun toUiState(state: PlaybackState): MiniPlayerUiState {
        val song = state.currentSong

        if (!state.hasQueue || song == null) return MiniPlayerUiState()

        return MiniPlayerUiState(primaryText = song.name, imageUri = song.getImageUri(), isVisible = true, isPlaying = state.isPlaying)
    }
}
