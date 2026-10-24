package dev.tcode.thinmp.view.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.playlist.PlaylistRegisterPopupView

@Composable
fun CommonLayoutView(isVisibleMiniPlayer: Boolean = true, content: @Composable ((showPlaylistRegisterPopup: (songId: SongId) -> Unit) -> Unit)) {
    val visiblePopup = remember { mutableStateOf(false) }
    // ポップアップ表示中にisVisibleMiniPlayerが変わると再composeされるので、rememberで保持する。
    // ローカル変数だとSongId("")に戻り、どの曲でもない行が書き込まれてしまう。
    val playlistRegisterSongId = remember { mutableStateOf(SongId("")) }
    val togglePopup = { visiblePopup.value = !visiblePopup.value }
    val showPlaylistRegisterPopup = { songId: SongId ->
        playlistRegisterSongId.value = songId
        togglePopup()
    }

    MiniPlayerLayoutView(isVisibleMiniPlayer) {
        content(showPlaylistRegisterPopup)

        if (visiblePopup.value) {
            PlaylistRegisterPopupView(playlistRegisterSongId.value, togglePopup)
        }
    }
}