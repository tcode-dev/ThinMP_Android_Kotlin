package dev.tcode.thinmp.view.screen

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.viewModel.SongsViewModel

@ExperimentalFoundationApi
@Composable
fun SongsScreen(viewModel: SongsViewModel = SongsViewModel(LocalContext.current), context: Context = LocalContext.current) {
    val uiState = viewModel.uiState

    Column {
        Text(text = "Songs")
        LazyColumn {
            items(uiState.songs) { song ->
                Row(modifier = Modifier.clickable {
                    viewModel.start(song)
                }) {
                    MediaRowView(song.name, song.artistName, song.getImageUri())
                }
            }
        }
    }
}
