package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.player.MusicPlayerListener
import dev.tcode.thinmp.service.SongsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SongsUiState(
    var songs: List<SongModel> = emptyList(), var isVisiblePlayer: Boolean = false
)

class SongsViewModel(application: Application) : AndroidViewModel(application), MusicPlayerListener, CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private var musicPlayer: MusicPlayer = MusicPlayer()
    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()

    init {
        musicPlayer.addEventListener(this)
        load(application)
    }

    fun start(index: Int) {
        musicPlayer.start(getApplication(), _uiState.asStateFlow().value.songs, index)

        println("Log: SongsViewModel start")
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

    override fun onBind() {
        updateIsVisiblePlayer()
    }

    private fun load(context: Context) {
        val repository = SongsService(context)
        val songs = repository.findAll()

        _uiState.update { currentState ->
            currentState.copy(
                songs = songs,
                isVisiblePlayer = musicPlayer.isServiceRunning()
            )
        }
    }

    private fun updateIsVisiblePlayer() {
        println("Log: SongsViewModel updateIsVisiblePlayer ${musicPlayer.isServiceRunning()}")
        _uiState.update { currentState ->
            currentState.copy(
                isVisiblePlayer = musicPlayer.isServiceRunning()
            )
        }
    }
}