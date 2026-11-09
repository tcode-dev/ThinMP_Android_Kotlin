package dev.tcode.thinmp.view.nav

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import dev.tcode.thinmp.constant.NavConstant

interface INavigator {
    fun back()
    fun backFromEvent()
    fun navigate(route: String)
    fun mainEdit()
    fun artistDetail(id: String)
    fun albumDetail(id: String)
    fun favoriteArtistsEdit()
    fun favoriteSongsEdit()
    fun playlistsEdit()
    fun playlistDetail(id: String)
    fun playlistDetailEdit(id: String)
    fun player()
}

/**
 * ユーザーの操作による遷移は、現在の画面がRESUMEDのときだけ行う。
 * 遷移のアニメーション中は前の画面も描かれていてタップを受け付けるので、
 * 連打すると同じ画面が2つ積まれたり、戻るが2回効いて最初の画面まで閉じたりする。
 * 遷移が始まると現在の画面はアニメーションが終わるまでRESUMEDにならないので、2回目は捨てられる。
 */
class Navigator(private val navController: NavController) : INavigator {
    override fun back() {
        if (!isResumed()) return

        navController.popBackStack()
    }

    /**
     * 保存の完了など、イベントを受けて画面を閉じるときに使う。
     * イベントはアプリがバックグラウンドにあってRESUMEDでないときにも届くので、back()と違って状態を確かめない。
     * 確かめると、イベントが捨てられて画面が閉じなくなる。
     */
    override fun backFromEvent() {
        navController.popBackStack()
    }

    override fun navigate(route: String) {
        if (!isResumed()) return

        navController.navigate(route)
    }

    override fun mainEdit() {
        navigate(NavConstant.MAIN_EDIT)
    }

    override fun artistDetail(id: String) {
        navigate("${NavConstant.ARTIST_DETAIL}/${id}")
    }

    override fun albumDetail(id: String) {
        navigate("${NavConstant.ALBUM_DETAIL}/${id}")
    }

    override fun favoriteArtistsEdit() {
        navigate(NavConstant.FAVORITE_ARTISTS_EDIT)
    }

    override fun favoriteSongsEdit() {
        navigate(NavConstant.FAVORITE_SONGS_EDIT)
    }

    override fun playlistDetail(id: String) {
        navigate("${NavConstant.PLAYLIST_DETAIL}/${id}")
    }

    override fun playlistDetailEdit(id: String) {
        navigate("${NavConstant.PLAYLIST_DETAIL_EDIT}/${id}")
    }

    override fun playlistsEdit() {
        navigate(NavConstant.PLAYLISTS_EDIT)
    }

    override fun player() {
        navigate(NavConstant.PLAYER)
    }

    private fun isResumed(): Boolean {
        return navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
    }
}
