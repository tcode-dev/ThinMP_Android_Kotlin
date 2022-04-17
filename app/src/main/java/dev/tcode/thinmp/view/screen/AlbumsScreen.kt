package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import dev.tcode.thinmp.view.grid.AlbumGridView
import dev.tcode.thinmp.viewModel.AlbumsViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel = AlbumsViewModel(context)

    Column{
        Text(text = "Albums")
        AlbumGridView(navController, viewModel.uiState.albums)
    }
}
