package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.service.FavoriteSongsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FavoriteSongsUiState(
    var songs: List<SongModel> = emptyList(), var isVisiblePlayer: Boolean = false
)

class FavoriteSongsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, MusicPlayerListener {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer = MusicPlayer(this)
    private val _uiState = MutableStateFlow(FavoriteSongsUiState())
    val uiState: StateFlow<FavoriteSongsUiState> = _uiState.asStateFlow()

    init {
        load(application)
        bindService()
    }

    fun load(context: Context) {
        val repository = FavoriteSongsService(context)
        val songs = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                songs = songs, isVisiblePlayer = musicPlayer.isServiceRunning()
            )
        }
    }

    fun start(index: Int) {
        musicPlayer.start(getApplication(), _uiState.asStateFlow().value.songs, index)
    }

    override fun onStop(context: Context) {
        musicPlayer.destroy(context)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
            bindService()
        } else {
            initialized = true
        }
    }

    override fun onBind() {
        updateIsVisiblePlayer()
    }

    private fun bindService() {
        if (musicPlayer.isServiceRunning()) {
            musicPlayer.bindService(getApplication())
        }
    }

    private fun updateIsVisiblePlayer() {
        _uiState.update { currentState ->
            currentState.copy(
                isVisiblePlayer = musicPlayer.isServiceRunning()
            )
        }
    }
}