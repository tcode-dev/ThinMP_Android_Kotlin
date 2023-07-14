package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.collapsingTopAppBar.MenuCollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistRemoveDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.layout.MiniPlayerLayoutView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.DropdownMenuView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.PlaylistsViewModel

@Composable
fun PlaylistsScreen(viewModel: PlaylistsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    MiniPlayerLayoutView {
        MenuCollapsingTopAppBarView(title = stringResource(R.string.playlists), dropdownMenus = {
            DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = { navigator.playlistsEdit() })
        }) {
            items(uiState.playlists) { playlist ->
                DropdownMenuView(dropdownContent = { callback ->
                    val callbackPlaylist = {
                        viewModel.deletePlaylist(playlist.id)
                        viewModel.load(context)
                        callback()
                    }
                    PlaylistRemoveDropdownMenuItemView(callbackPlaylist)
                    ShortcutDropdownMenuItemView(playlist.id, callback)
                }) { callback ->
                    PlainRowView(playlist.primaryText, Modifier.pointerInput(playlist.url) {
                        detectTapGestures(onLongPress = { callback() }, onTap = { navigator.playlistDetail(playlist.id.id) })
                    })
                }
            }
        }
    }
}