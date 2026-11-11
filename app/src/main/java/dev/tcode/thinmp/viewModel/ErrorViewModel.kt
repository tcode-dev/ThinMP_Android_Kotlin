package dev.tcode.thinmp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ErrorViewのview model。
 *
 * [contentOwner]は、ErrorViewが包む画面のViewModelを置くストア。
 * NavHostはLocalViewModelStoreOwnerのストアに戻る履歴の各画面のViewModelを置き、
 * NavHostがcompositionから外れてもそれを消さない。
 * Activityのストアのままだと、再読み込みのたびに前の画面のViewModelが残り、
 * songRemovedの購読を続けて読み込みを繰り返す。
 * そのため、再読み込みでストアごと捨てる。
 * 画面の回転でも消えないように、compositionではなくこのview modelに持たせている。
 */
@HiltViewModel
class ErrorViewModel @Inject constructor(private val mediaStoreFailure: MediaStoreFailure) : ViewModel() {
    val failed: StateFlow<Boolean> = mediaStoreFailure.failed

    var contentOwner: ViewModelStoreOwner = createOwner()
        private set

    /**
     * 先にストアを替えてから失敗の状態を戻す。
     * 状態が戻るとErrorViewが[contentOwner]で画面を描き直すからだ。
     */
    fun reload() {
        contentOwner.viewModelStore.clear()
        contentOwner = createOwner()
        mediaStoreFailure.reset()
    }

    override fun onCleared() {
        contentOwner.viewModelStore.clear()
    }

    private fun createOwner(): ViewModelStoreOwner {
        return object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
}
