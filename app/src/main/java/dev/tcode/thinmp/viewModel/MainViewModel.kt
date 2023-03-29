package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.model.media.ShortcutModel
import dev.tcode.thinmp.service.MainService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    var menu: List<MainMenuEnum> = emptyList(), var albums: List<AlbumModel> = emptyList(), var shortcuts: List<ShortcutModel> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private var initialized: Boolean = false
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        load(application)
    }

    override fun onResume(context: Context) {
        if (initialized) {
            load(context)
        } else {
            initialized = true
        }
    }

    fun load(context: Context) {
        val service = MainService(context)
        val menu = service.getMenu()
        val albums = service.getRecentlyAlbums()
        val shortcuts = service.getShortcuts()

        _uiState.update { currentState ->
            currentState.copy(
                menu = menu, albums = albums, shortcuts = shortcuts
            )
        }
    }
}