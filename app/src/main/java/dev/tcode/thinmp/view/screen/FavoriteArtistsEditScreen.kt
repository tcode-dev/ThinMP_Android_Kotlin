package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.swipe.SwipeToDismissView
import dev.tcode.thinmp.view.util.OnEvent
import dev.tcode.thinmp.viewModel.FavoriteArtistsEditViewModel

@Composable
fun FavoriteArtistsEditScreen(viewModel: FavoriteArtistsEditViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val callback = { viewModel.save() }

    OnEvent(viewModel.saved) { navigator.backFromEvent() }

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(uiState.loaded, callback) {
            items(uiState.artists, key = { it.id }) { artist ->
                SwipeToDismissView(callback = { viewModel.removeArtist(artist.artistId) }) {
                    PlainRowView(artist.name)
                }
            }
        }
    }
}