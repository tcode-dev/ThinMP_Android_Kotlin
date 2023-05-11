package dev.tcode.thinmp.view.nav

import androidx.navigation.NavController
import dev.tcode.thinmp.constant.NavConstant

class Navigator(private val navController: NavController) : INavigator {
    override fun back() {
        navController.popBackStack()
    }

    override fun mainEdit() {
        navController.navigate(NavConstant.MAIN_EDIT)
    }

    override fun artistDetail(id: String) {
        navController.navigate("${NavConstant.ARTIST_DETAIL}/${id}")
    }

    override fun albumDetail(id: String) {
        navController.navigate("${NavConstant.ALBUM_DETAIL}/${id}")
    }

    override fun favoriteArtistsEdit() {
        navController.navigate(NavConstant.FAVORITE_ARTISTS_EDIT)
    }

    override fun favoriteSongsEdit() {
        navController.navigate(NavConstant.FAVORITE_SONGS_EDIT)
    }

    override fun playlistDetail(id: String) {
        navController.navigate("${NavConstant.PLAYLIST_DETAIL}/${id}")
    }

    override fun playlistDetailEdit(id: String) {
        navController.navigate("${NavConstant.PLAYLIST_DETAIL_EDIT}/${id}")
    }

    override fun player() {
        navController.navigate(NavConstant.PLAYER)
    }
}