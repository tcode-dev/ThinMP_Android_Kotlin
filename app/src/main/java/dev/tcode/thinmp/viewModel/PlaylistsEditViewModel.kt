package dev.tcode.thinmp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.tcode.thinmp.model.media.PlaylistModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.service.PlaylistsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

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

@HiltViewModel
class PlaylistsEditViewModel @Inject constructor(application: Application) : AndroidViewModel(application), PlaylistRegister {
    private var loadJob: Job? = null
    private var saveJob: Job? = null

    private val _uiState = MutableStateFlow(PlaylistsEditUiState())
    val uiState: StateFlow<PlaylistsEditUiState> = _uiState.asStateFlow()
    val saved = OneShotEvent<Unit>()

    init {
        load()
    }

    /**
     * 読み込むのはinitの1回だけで、onResumeでは読み直さない。
     * 読み直すと、アプリに戻ったときに保存前の編集がDBの内容で上書きされてしまう。
     */
    private fun load() {
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

    /**
     * 位置ではなくidで消す。
     * スワイプが確定するまでの間に一覧が入れ替わっても、別の行を消したり範囲外で落ちたりしない。
     */
    fun removePlaylist(id: PlaylistId) {
        _uiState.update { currentState ->
            currentState.copy(
                playlists = currentState.playlists.filter { it.id != id }
            )
        }
    }

    /**
     * 完了ボタンは読み込みが終わるまで無効なので、
     * ここに来るのは状態が画面に反映される前にタップがすり抜けたときだけ。
     * そのときは保存せず、保存済みの通知も出さない。
     * 何も保存していないのに画面が閉じるより、タップが効かなかったように見えるほうがよい。
     *
     * 保存は1つの画面で1回だけで、終わった後もやり直さない。
     * 閉じる遷移のアニメーション中も画面はタップを受け付け、OnEventも残っているので、
     * 2回目を保存するとbackFromEvent()がもう一度呼ばれ、前の画面まで閉じてしまう。
     */
    fun save() {
        if (saveJob != null) return
        if (!uiState.value.loaded) return

        saveJob = viewModelScope.launch {
            reorderPlaylists(uiState.value.playlists.map { it.id })
            saved.emit(Unit)
        }
    }
}