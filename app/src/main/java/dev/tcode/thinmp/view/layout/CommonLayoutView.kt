package dev.tcode.thinmp.view.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.playlist.PlaylistPopupView
import dev.tcode.thinmp.view.util.miniPlayerHeight

@Composable
fun CommonLayoutView(content: @Composable ((showPlaylistPopup: (songId: SongId) -> Unit) -> Unit)) {
    val miniPlayerHeight = miniPlayerHeight()
    val visiblePopup = remember { mutableStateOf(false) }
    var playlistRegisterSongId = SongId("")
    val togglePopup = { visiblePopup.value = !visiblePopup.value }
    val showPlaylistPopup = { songId: SongId ->
        playlistRegisterSongId = songId
        togglePopup()
    }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        content(showPlaylistPopup)

        if (visiblePopup.value) {
            PlaylistPopupView(playlistRegisterSongId, togglePopup)
        }

        Box(modifier = Modifier.constrainAs(miniPlayer) {
            top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
        }) {
            MiniPlayerView()
        }
    }
}