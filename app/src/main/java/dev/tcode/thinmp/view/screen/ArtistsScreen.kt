package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.viewModel.ArtistsViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistsScreen() {
    val context = LocalContext.current
    val vm = ArtistsViewModel(context)

    Column{
        Text(text = "Artists")
        LazyColumn{
            items(vm.uiState.artists) { artist ->
                PlainRowView(artist.name)
            }
        }
    }
}
