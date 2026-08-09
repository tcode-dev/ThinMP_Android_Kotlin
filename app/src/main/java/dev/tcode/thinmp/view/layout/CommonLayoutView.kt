package dev.tcode.thinmp.view.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.playlist.PlaylistRegisterPopupView

@Composable
fun CommonLayoutView(isVisibleMiniPlayer: Boolean = true, content: @Composable ((showPlaylistRegisterPopup: (songId: SongId) -> Unit) -> Unit)) {
    val visiblePopup = remember { mutableStateOf(false) }
    // The id has to survive a recomposition of CommonLayoutView itself, which happens while the
    // popup is open whenever isVisibleMiniPlayer flips. A plain local was reinitialised to
    // SongId("") there, and adding from the popup then wrote a row that resolves to no song.
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