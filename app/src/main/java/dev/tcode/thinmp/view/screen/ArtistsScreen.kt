package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.tcode.thinmp.view.list.FlatListView
import dev.tcode.thinmp.viewModel.ArtistsViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistsScreen() {
    val context = LocalContext.current
    val viewModel = ArtistsViewModel(context)

    Column{
        Text(text = "Albums")
        FlatListView(viewModel.uiState.artists)
    }
}
