package dev.tcode.thinmp.view.screen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.player.MusicPlayer
import dev.tcode.thinmp.view.image.ImageView
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
                    val musicPlayer = MusicPlayer()
                    musicPlayer.start(context, song)
                }) {
                    MediaRowView(song.name, song.artistName, song.getUri())
                }
            }
        }
    }
}
