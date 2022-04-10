package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.tcode.thinmp.view.list.FlatListView
import dev.tcode.thinmp.viewModel.SongsViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistsScreen() {
    val context = LocalContext.current
//    val viewModel = SongsViewModel(context)

    Column{
        Text(text = "Artists")
//        FlatListView(viewModel.uiState.songs)
    }
}
