package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import dev.tcode.thinmp.config.MainMenuVisibilityState
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.service.MainService
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserverListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainEditUiState(
    var menu: List<MainMenuEnum> = emptyList()
)

class MainEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private val _uiState = MutableStateFlow(MainEditUiState())
    val uiState: StateFlow<MainEditUiState> = _uiState.asStateFlow()
    var count = 0

    init {
        load(application)
    }

    fun load(context: Context) {
        val service = MainService(context)
        val menu = service.getMenu()

        _uiState.update { currentState ->
            currentState.copy(
                menu = menu
            )
        }
    }

    fun setMainMenuVisibility(target: String, context: Context) {
        val service = MainService(context)
        val menu = service.getMenu()
        val updated = menu.map {
            if (it.key == target) {
                it.visibility = MainMenuVisibilityState.INVISIBLE
            }
            it
        }

//        val data = if (count == 0){ listOf(updated[0])  } else { updated}
//        count += 1

        val map = _uiState.value.menu.map {
            if (it.key == target) {
                it.visibility = MainMenuVisibilityState.INVISIBLE
            }
            it
        }
        _uiState.update { currentState ->
            currentState.copy(
                menu = map
            )
        }
//        _uiState.update { currentState ->
//            currentState.copy(
//                menu = updated
//            )
//        }
//        _uiState.update { currentState ->
//            val menu = currentState.menu.map{
//                if (it.key == target) {
//                    it.visibility = MainMenuVisibilityState.INVISIBLE
//                }
//                it
//            }
//
//            currentState.copy(
//                menu = menu
//            )
//        }
    }

    fun save() {

    }
}