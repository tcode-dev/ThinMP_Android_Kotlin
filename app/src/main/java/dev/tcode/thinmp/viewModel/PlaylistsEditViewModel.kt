package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.service.PlaylistsService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * `loaded`はload()が実際のデータを入れるまでfalseで、その間は完了ボタンを無効にする。
 * save()はこの状態をそのまま保存し、reorder/replaceAllは一覧にないものを削除するので、
 * 読み込み前に保存するとテーブルの中身がすべて消えてしまう。
 *
 * 一覧が空かどうかでは判断できない。
 * ユーザーがすべてスワイプで消した場合も一覧は空になる。
 */
data class PlaylistsEditUiState(
    val playlists: List<PlaylistModel> = emptyList(), val loaded: Boolean = false
)

class PlaylistsEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener, PlaylistRegister {
    private var initialized: Boolean = false
    private var loadJob: Job? = null
    private var saveJob: Job? = null

    private val _uiState = MutableStateFlow(PlaylistsEditUiState())
    val uiState: StateFlow<PlaylistsEditUiState> = _uiState.asStateFlow()
    val saved = OneShotEvent<Unit>()

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

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val service = PlaylistsService(getApplication())
            val playlists = service.findAll()

            _uiState.update { currentState ->
                currentState.copy(
                    playlists = playlists, loaded = true
                )
            }
        }
    }

    fun removePlaylist(index: Int) {
        _uiState.update { currentState ->
            val list = currentState.playlists.toMutableList()

            list.removeAt(index)

            currentState.copy(
                playlists = list
            )
        }
    }

    /**
     * 完了ボタンは読み込みが終わるまで無効なので、
     * ここに来るのは状態が画面に反映される前にタップがすり抜けたときだけ。
     * そのときは保存せず、保存済みの通知も出さない。
     * 何も保存していないのに画面が閉じるより、タップが効かなかったように見えるほうがよい。
     */
    fun save() {
        if (saveJob?.isActive == true) return
        if (!uiState.value.loaded) return

        saveJob = viewModelScope.launch {
            reorderPlaylists(uiState.value.playlists.map { it.id })
            saved.emit(Unit)
        }
    }
}