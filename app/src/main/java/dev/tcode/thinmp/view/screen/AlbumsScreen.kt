package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.topbar.ListTopbarView
import dev.tcode.thinmp.view.util.EmptyTopbarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.AlbumsViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumsScreen(
    navController: NavHostController,
    viewModel: AlbumsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    Box {
        val lazyGridState = rememberLazyGridState()
        val itemSize: Dp = (LocalConfiguration.current.screenWidthDp.dp / 2)

        Box(Modifier.zIndex(3F)) {
            ListTopbarView(navController, "Albums", lazyGridState.firstVisibleItemScrollOffset)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = lazyGridState
        ) {
            item {
                EmptyTopbarView()
            }
            item {
                EmptyTopbarView()
            }
            itemsIndexed(uiState.albums) { index, album ->
                GridCellView(index, 2, itemSize) {
                    AlbumCellView(
                        album.name,
                        album.artistName,
                        album.getUri(),
                        Modifier
                            .clickable {
                                navController.navigate("albumDetail/${album.id}")
                            }
                    )
                }
            }
        }
    }
}
