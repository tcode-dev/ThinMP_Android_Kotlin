package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.viewModel.SongsViewModel

@ExperimentalFoundationApi
@Composable
fun SongsScreen() {
    val context = LocalContext.current
    val viewModel = SongsViewModel(context)

    Column{
        Text(text = "Songs")
        LazyColumn{
            items(viewModel.uiState.songs) { song ->
                MediaRowView(song.name, song.artistName, song.getUri())
            }
        }
    }
}
