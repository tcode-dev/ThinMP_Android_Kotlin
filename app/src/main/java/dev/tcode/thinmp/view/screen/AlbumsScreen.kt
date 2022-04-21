package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
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
fun AlbumsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel = AlbumsViewModel(context)

    Box() {
        Box(Modifier.zIndex(1F)) {
            ListTopbarView("Albums")
        }
        AlbumGridView(navController, viewModel.uiState.albums)
    }
}
