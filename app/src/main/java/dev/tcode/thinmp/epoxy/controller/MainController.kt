package dev.tcode.thinmp.epoxy.controller

import com.airbnb.epoxy.TypedEpoxyController
import dev.tcode.thinmp.constant.MainMenuEnum
import dev.tcode.thinmp.epoxy.model.mainMenu
import dev.tcode.thinmp.viewModel.MainViewModel

class MainController: TypedEpoxyController<MainViewModel>() {
    override fun buildModels(vm: MainViewModel) {
        buildMenu(vm.menuList, vm.mainMenuSpanSize)
    }

    private fun buildMenu(menuList: Array<MainMenuEnum>, spanSize: Int) {
        menuList
            .forEach { menu ->
                mainMenu {
                    id(menu.key)
                    primaryText(menu.label)
                    spanSizeOverride { _, _, _ -> spanSize }
                }
            }
    }
}
