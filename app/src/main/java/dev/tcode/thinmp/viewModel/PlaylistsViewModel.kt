package dev.tcode.thinmp.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.R
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.service.PlaylistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlaylistsUiState(
    var playlists: List<PlaylistModel> = emptyList(),
    /** Playlists the popup's song is already in. Empty until a song is named. */
    var registeredPlaylistIds: Set<PlaylistId> = emptySet()
)

class PlaylistsViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, PlaylistRegister {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    // The register popup is opened for one song, and the list has to mark the playlists that song
    // is already in. onResume reloads without being told the song again, so it is kept here.
    private var songId: SongId? = null
    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    override fun onResume() {
        if (initialized) {
            load()
        } else {
            initialized = true
        }
    }

    fun load(songId: SongId? = null) {
        if (songId != null) {
            this.songId = songId
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = PlaylistsService(getApplication())
            val playlists = service.findAll()
            val registeredPlaylistIds = this@PlaylistsViewModel.songId?.let { service.findRegisteredPlaylistIds(it) } ?: emptySet()

            _uiState.update { currentState ->
                currentState.copy(
                    playlists = playlists, registeredPlaylistIds = registeredPlaylistIds
                )
            }
        }
    }

    // The popup and the dropdown close as soon as these are invoked, so the writes run in
    // viewModelScope rather than in the caller's composition scope.
    fun create(songId: SongId, name: String) {
        viewModelScope.launch { createPlaylist(songId, name) }
    }

    fun addSong(playlistId: PlaylistId, songId: SongId) {
        viewModelScope.launch {
            if (addSongToPlaylist(playlistId, songId)) return@launch

            // The popup is gone by the time the write reports back, so there is no composition
            // left to raise this from. viewModelScope runs on the main thread, which is where a
            // Toast has to be shown.
            Toast.makeText(getApplication(), R.string.already_added_to_playlist, Toast.LENGTH_SHORT).show()
        }
    }

    fun delete(playlistId: PlaylistId) {
        viewModelScope.launch {
            deletePlaylist(playlistId)
            load()
        }
    }
}