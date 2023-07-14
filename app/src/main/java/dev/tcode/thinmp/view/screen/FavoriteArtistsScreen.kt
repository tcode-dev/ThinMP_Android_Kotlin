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
import dev.tcode.thinmp.view.dropdownMenu.FavoriteArtistDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.layout.MiniPlayerLayoutView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.DropdownMenuView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.FavoriteArtistsViewModel

@Composable
fun FavoriteArtistsScreen(viewModel: FavoriteArtistsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    MiniPlayerLayoutView {
        MenuCollapsingTopAppBarView(title = stringResource(R.string.favorite_artists), dropdownMenus = {
            DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = { navigator.favoriteArtistsEdit() })
        }) {
            items(uiState.artists) { artist ->
                DropdownMenuView(dropdownContent = { callback ->
                    val close = {
                        callback()
                        viewModel.load(context)
                    }
                    FavoriteArtistDropdownMenuItemView(artist.artistId, close)
                    ShortcutDropdownMenuItemView(artist.artistId, callback)
                }) { callback ->
                    PlainRowView(artist.name, Modifier.pointerInput(artist.url) {
                        detectTapGestures(onLongPress = { callback() }, onTap = { navigator.artistDetail(artist.id) })
                    })
                }
            }
        }
    }
}