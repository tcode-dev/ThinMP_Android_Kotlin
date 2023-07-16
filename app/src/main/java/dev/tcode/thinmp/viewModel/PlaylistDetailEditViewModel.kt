package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.service.PlaylistDetailService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PlaylistDetailEditUiState(
    var primaryText: String = "", var songs: List<SongModel> = emptyList()
)

@HiltViewModel
class PlaylistDetailEditViewModel @Inject constructor(
    application: Application, savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), CustomLifecycleEventObserverListener, PlaylistRegister {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(PlaylistDetailEditUiState())
    val uiState: StateFlow<PlaylistDetailEditUiState> = _uiState.asStateFlow()
    val id: PlaylistId

    init {
        id = PlaylistId(savedStateHandle.get<String>("id").toString())

        load(application)
    }

    fun removeSong(index: Int) {
        _uiState.update { currentState ->
            val list = currentState.songs.toMutableList()

            list.removeAt(index)

            currentState.copy(
                songs = list
            )
        }
    }

    fun changeName(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                primaryText = name
            )
        }
    }

    fun update() {
        val songIds = uiState.value.songs.map { it.songId }

        updatePlaylist(id, uiState.value.primaryText, songIds)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }

    private fun load(context: Context) {
        val service = PlaylistDetailService(context)
        val playlist = service.findById(id)

        if (playlist != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    primaryText = playlist.primaryText, songs = playlist.songs
                )
            }
        }
    }
}