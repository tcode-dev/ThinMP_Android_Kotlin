package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import dev.tcode.thinmp.view.grid.AlbumGridView
import dev.tcode.thinmp.view.topbar.ListTopbarView
import dev.tcode.thinmp.viewModel.AlbumsViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumsScreen(
    navController: NavHostController,
    viewModel: AlbumsViewModel = AlbumsViewModel(LocalContext.current)
) {
    val uiState = viewModel.uiState

    Box {
        val lazyGridState = rememberLazyGridState()

        Box(Modifier.zIndex(3F)) {
            ListTopbarView("Albums", lazyGridState.firstVisibleItemScrollOffset)
        }
        AlbumGridView(navController, uiState.albums, lazyGridState)
    }
}
