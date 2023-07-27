package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.service.SongsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SongsUiState(
    var songs: List<SongModel> = emptyList()
)

class SongsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer = MusicPlayer()
    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()

    init {

        musicPlayer.bindService(application)
        load(application)
    }

    fun start(index: Int) {
        musicPlayer.start(_uiState.asStateFlow().value.songs, index)
    }

    override fun onStop(context: Context) {
        musicPlayer.destroy(context)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            musicPlayer.bindService(context)
            load(context)
        } else {
            initialized = true
        }
    }

    private fun load(context: Context) {
        val repository = SongsService(context)
        val songs = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                songs = songs
            )
        }
    }
}