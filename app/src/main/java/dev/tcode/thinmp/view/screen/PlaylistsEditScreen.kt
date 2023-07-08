package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.swipe.SwipeToDismissView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopAppBarView
import dev.tcode.thinmp.viewModel.PlaylistsEditViewModel

@Composable
fun PlaylistsEditScreen(viewModel: PlaylistsEditViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val doneCallback = {
        viewModel.update()
        navigator.back()
    }

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(doneCallback) {
            item {
                EmptyTopAppBarView()
            }
            itemsIndexed(uiState.playlists) { index, playlist ->
                SwipeToDismissView(callback = { viewModel.removePlaylist(index) }) {
                    PlainRowView(playlist.primaryText)
                }
            }
            item {
                EmptyMiniPlayerView()
            }
        }
    }
}