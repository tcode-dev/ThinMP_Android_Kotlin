package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.collapsingTopAppBar.MenuCollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.FavoriteSongDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistDropdownMenuItemView
import dev.tcode.thinmp.view.layout.CommonLayoutView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.DropdownMenuView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.FavoriteSongsViewModel
import java.util.UUID

@Composable
fun FavoriteSongsScreen(viewModel: FavoriteSongsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    CommonLayoutView(uiState.isVisiblePlayer) { showPlaylistRegisterPopup ->
        MenuCollapsingTopAppBarView(title = stringResource(R.string.favorite_songs), dropdownMenus = {
            DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = { navigator.favoriteSongsEdit() })
        }) {
            itemsIndexed(uiState.songs) { index, song ->
                DropdownMenuView(dropdownContent = { callback ->
                    val callbackFavorite = {
                        callback()
                        viewModel.load()
                    }
                    val callbackPlaylist = {
                        showPlaylistRegisterPopup(song.songId)
                        callback()
                    }
                    FavoriteSongDropdownMenuItemView(song.songId, callbackFavorite)
                    PlaylistDropdownMenuItemView(callbackPlaylist)
                }) { callback ->
                    MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.pointerInput(UUID.randomUUID()) {
                        detectTapGestures(onLongPress = { callback() }, onTap = { viewModel.start(index) })
                    })
                }
            }
        }
    }
}