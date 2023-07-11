package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.collapsingTopAppBar.ColumnCollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.FavoriteArtistDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.layout.MiniPlayerLayoutView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.DropdownMenuView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.ArtistsViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistsScreen(viewModel: ArtistsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    MiniPlayerLayoutView {
        ColumnCollapsingTopAppBarView(stringResource(R.string.artists)) {
            items(uiState.artists) { artist ->
                DropdownMenuView(dropdownContent = { callback ->
                    FavoriteArtistDropdownMenuItemView(artist.artistId, callback)
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