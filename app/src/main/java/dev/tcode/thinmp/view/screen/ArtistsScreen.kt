package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.topbar.ListTopbarView
import dev.tcode.thinmp.viewModel.ArtistsViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistsScreen(
    navController: NavHostController,
    viewModel: ArtistsViewModel = ArtistsViewModel(LocalContext.current)
) {
    val uiState = viewModel.uiState

    Box {
        val lazyListState = rememberLazyListState()

        Box(Modifier.zIndex(3F)) {
            ListTopbarView(navController, "Artists", lazyListState.firstVisibleItemScrollOffset)
        }
        LazyColumn(state = lazyListState) {
            item {
                SpacerView()
            }
            items(uiState.artists) { artist ->
                PlainRowView(artist.name, modifier = Modifier
                    .clickable {
                        navController.navigate("artistDetail/${artist.id}")
                    })
            }
        }
    }
}
