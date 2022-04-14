package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.viewModel.AlbumDetailViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumDetailScreen(id: String) {
    val context = LocalContext.current
    val viewModel = AlbumDetailViewModel(context, id)

    Column{
        viewModel.uiState.album?.getUri()?.let { ImageView(uri = it) }
        viewModel.uiState.album?.name?.let { Text(it) }
        viewModel.uiState.album?.artistName?.let { Text(it) }
    }
}
