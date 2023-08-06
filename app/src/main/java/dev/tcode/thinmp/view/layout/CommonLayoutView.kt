package dev.tcode.thinmp.view.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.playlist.PlaylistRegisterPopupView
import dev.tcode.thinmp.view.util.miniPlayerHeight

@Composable
fun CommonLayoutView(isVisibleMiniPlayer: Boolean = true, content: @Composable ((showPlaylistRegisterPopup: (songId: SongId) -> Unit) -> Unit)) {
    val miniPlayerHeight = miniPlayerHeight()
    val visiblePopup = remember { mutableStateOf(false) }
    var playlistRegisterSongId = SongId("")
    val togglePopup = { visiblePopup.value = !visiblePopup.value }
    val showPlaylistRegisterPopup = { songId: SongId ->
        playlistRegisterSongId = songId
        togglePopup()
    }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        content(showPlaylistRegisterPopup)

        if (visiblePopup.value) {
            PlaylistRegisterPopupView(playlistRegisterSongId, togglePopup)
        }

        if (isVisibleMiniPlayer) {
            Box(modifier = Modifier.constrainAs(miniPlayer) {
                top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
            }) {
                MiniPlayerView()
            }
        }
    }
}