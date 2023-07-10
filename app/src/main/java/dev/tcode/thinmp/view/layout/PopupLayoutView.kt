package dev.tcode.thinmp.view.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.playlist.PlaylistRegisterPopupView

@Composable
fun PopupLayoutView(content: @Composable() (BoxScope.(togglePopup: () -> Unit, setPlaylistRegisterSongId: (songId: SongId) -> Unit) -> Unit)) {
    val visiblePopup = remember { mutableStateOf(false) }
    var playlistRegisterSongId = SongId("")
    val togglePopup = { visiblePopup.value = false }
    val setPlaylistRegisterSongId = { songId: SongId -> playlistRegisterSongId = songId }

    ConstraintLayout(Modifier.fillMaxSize()) {
        Box {
            content(togglePopup, setPlaylistRegisterSongId)
        }
        if (visiblePopup.value) {
            PlaylistRegisterPopupView(playlistRegisterSongId, { visiblePopup.value = false })
        }
    }
}