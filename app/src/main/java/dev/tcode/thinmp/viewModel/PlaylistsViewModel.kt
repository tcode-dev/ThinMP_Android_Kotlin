package dev.tcode.thinmp.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

data class PlaylistsUiState(
    val playlists: List<PlaylistModel> = emptyList(),
    /**
     * `registeredSongId`の曲が入っているプレイリスト。
     * 曲が決まるまでは空。
     */
    val registeredPlaylistIds: Set<PlaylistId> = emptySet(),
    /**
     * `registeredPlaylistIds`をどの曲について読んだか。
     * ViewModelは画面のback stack entryに残るので、別の曲でポップアップを開き直した直後は前の曲の結果が入っている。
     * ポップアップはこれが自分の曲と一致するときだけ印を付ける。
     */
    val registeredSongId: SongId? = null
)

@HiltViewModel
class PlaylistsViewModel @Inject constructor(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, PlaylistRegister {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    // 登録ポップアップでは、その曲が入っているプレイリストに印を付ける。
    // onResumeでの再読み込みでは曲が渡されないので、ここに保持しておく。
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

        val registeredSongId = this.songId

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = PlaylistsService(getApplication())
            val playlists = service.findAll()
            val registeredPlaylistIds = registeredSongId?.let { service.findRegisteredPlaylistIds(it) } ?: emptySet()

            _uiState.update { currentState ->
                currentState.copy(
                    playlists = playlists, registeredPlaylistIds = registeredPlaylistIds, registeredSongId = registeredSongId
                )
            }
        }
    }

    // ポップアップやドロップダウンは呼んだ直後に閉じるので、
    // 書き込みはcompositionのスコープではなくviewModelScopeで行う。
    fun create(songId: SongId, name: String) {
        viewModelScope.launch { createPlaylist(songId, name) }
    }

    fun addSong(playlistId: PlaylistId, songId: SongId) {
        viewModelScope.launch {
            if (addSongToPlaylist(playlistId, songId)) return@launch

            // ポップアップはもう閉じているので、ここでToastを出す。viewModelScopeはメインスレッドで動く。
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