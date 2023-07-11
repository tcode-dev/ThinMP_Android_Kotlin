package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.collapsingTopAppBar.GridCollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.layout.MiniPlayerLayoutView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.DropdownRowView
import dev.tcode.thinmp.view.util.*
import dev.tcode.thinmp.viewModel.AlbumsViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val spanCount: Int = gridSpanCount()

    CustomLifecycleEventObserver(viewModel)

    MiniPlayerLayoutView {
        GridCollapsingTopAppBarView(title = stringResource(R.string.albums), columns = CustomGridCellsFixed(spanCount), spanCount) {
            itemsIndexed(uiState.albums) { index, album ->
                DropdownRowView(dropdownContent = { callback ->
                    ShortcutDropdownMenuItemView(album.albumId, callback)
                }) { callback ->
                    GridCellView(index, spanCount) {
                        AlbumCellView(album.name, album.artistName, album.getImageUri(), Modifier.pointerInput(album.url) {
                            detectTapGestures(onLongPress = { callback() }, onTap = { navigator.albumDetail(album.id) })
                        })
                    }
                }
            }
        }
    }
}