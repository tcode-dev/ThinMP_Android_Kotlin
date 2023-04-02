package dev.tcode.thinmp.viewModel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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
    var menu: MainMenuEnum
)

class MainEditViewModel(application: Application) : AndroidViewModel(application), CustomLifecycleEventObserverListener {
    private val _itemsList = MutableStateFlow(listOf<MainMenuEnum>())
    val itemsList: StateFlow<List<MainMenuEnum>> get() = _itemsList


    init {
        load(application)


    }

    fun load(context: Context) {
        val service = MainService(context)
        val menu = service.getMenu()

        _itemsList.value = menu
    }

    fun setMainMenuVisibility(target: String, context: Context) {
        val service = MainService(context)
        val menu = service.getMenu()
        val map = menu.map {
            if (it.key == target) {
                it.visibility = MainMenuVisibilityState.INVISIBLE
            }
            it
        }

        _itemsList.value = emptyList()





//        _uiState.update { currentState ->
//            currentState.copy(
//                menu = map
//            )
//        }
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