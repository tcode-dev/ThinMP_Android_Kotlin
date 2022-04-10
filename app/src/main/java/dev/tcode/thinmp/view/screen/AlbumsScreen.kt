package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.tcode.thinmp.view.list.GridListView
import dev.tcode.thinmp.viewModel.AlbumsViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumsScreen() {
    val context = LocalContext.current
    val viewModel = AlbumsViewModel(context)

    Column{
        Text(text = "Albums")
        GridListView(viewModel.uiState.albums)
    }
}
