package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.swipe.SwipeToDismissView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.FavoriteArtistsEditViewModel

@Composable
fun FavoriteArtistsEditScreen(viewModel: FavoriteArtistsEditViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val callback = {
        viewModel.update()
        navigator.back()
    }

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(callback) {
            itemsIndexed(uiState.artists) { index, artist ->
                SwipeToDismissView(callback = { viewModel.removeArtist(index) }) {
                    PlainRowView(artist.name)
                }
            }
        }
    }
}