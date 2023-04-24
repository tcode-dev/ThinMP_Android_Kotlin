package dev.tcode.thinmp.view.nav

import androidx.navigation.NavController
import dev.tcode.thinmp.constant.NavConstant

class Navigator(private val navController: NavController) : INavigator {
    override fun back() {
        navController.popBackStack()
    }

    override fun albumDetail(id: String) {
        navController.navigate("${NavConstant.ALBUM_DETAIL}/${id}")
    }

    override fun player() {
        navController.navigate(NavConstant.PLAYER)
    }
}