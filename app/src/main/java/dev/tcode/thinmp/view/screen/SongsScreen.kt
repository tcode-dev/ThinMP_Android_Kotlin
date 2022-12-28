package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import dev.tcode.thinmp.view.grid.SpacerView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.topbar.ListTopbarView
import dev.tcode.thinmp.viewModel.SongsViewModel

@ExperimentalFoundationApi
@Composable
fun SongsScreen(
    navController: NavHostController,
    viewModel: SongsViewModel = SongsViewModel(LocalContext.current)
) {
    val uiState = viewModel.uiState

    Box {
        val lazyListState = rememberLazyListState()

        Box(Modifier.zIndex(3F)) {
            ListTopbarView(navController, "Songs", lazyListState.firstVisibleItemScrollOffset)
        }
        LazyColumn(state = lazyListState) {
            item {
                SpacerView()
            }
            itemsIndexed(uiState.songs) { index, song ->
                Column(modifier = Modifier.clickable {
                    viewModel.start(index)
                }) {
                    MediaRowView(song.name, song.artistName, song.getImageUri())
                }
            }
        }
    }
}
