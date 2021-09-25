package dev.tcode.thinmp.viewModel

import dev.tcode.thinmp.constant.MainMenuEnum

class MainViewModel {
    val mainMenuSpanSize = 2
    val layoutSpanSize = 2
    lateinit var menuList: Array<MainMenuEnum>

    fun load() {
        menuList = MainMenuEnum.values()
    }
}