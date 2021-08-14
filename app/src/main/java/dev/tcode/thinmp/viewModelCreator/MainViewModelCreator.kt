package dev.tcode.thinmp.viewModelCreator

import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.viewModel.MainViewModel

class MainViewModelCreator {
    fun create(): MainViewModel {
        return MainViewModel(MainMenuEnum.values(), 2, 2)
    }
}