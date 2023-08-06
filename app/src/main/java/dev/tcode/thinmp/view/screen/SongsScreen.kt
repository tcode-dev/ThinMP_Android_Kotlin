package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.collapsingTopAppBar.ColumnCollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.FavoriteSongDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistDropdownMenuItemView
import dev.tcode.thinmp.view.layout.CommonLayoutView
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.row.DropdownMenuView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.miniPlayerHeight
import dev.tcode.thinmp.viewModel.SongsViewModel

@Composable
fun SongsScreen(viewModel: SongsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    CommonLayoutView(uiState.isVisiblePlayer) { showPlaylistRegisterPopup ->
        ColumnCollapsingTopAppBarView(stringResource(R.string.songs)) {
            itemsIndexed(uiState.songs) { index, song ->
                DropdownMenuView(dropdownContent = { callback ->
                    val callbackPlaylist = {
                        showPlaylistRegisterPopup(song.songId)
                        callback()
                    }
                    FavoriteSongDropdownMenuItemView(song.songId, callback)
                    PlaylistDropdownMenuItemView(callbackPlaylist)
                }) { callback ->
                    MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.pointerInput(index) {
                        detectTapGestures(onLongPress = { callback() }, onTap = { viewModel.start(index) })
                    })
                }
            }
        }
    }
}