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
import dev.tcode.thinmp.viewModel.PlaylistsEditViewModel

@Composable
fun PlaylistsEditScreen(viewModel: PlaylistsEditViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val doneCallback = { viewModel.save() }

    OnEvent(viewModel.saved) { navigator.backFromEvent() }

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(uiState.loaded, doneCallback) {
            items(uiState.playlists, key = { it.id.id }) { playlist ->
                SwipeToDismissView(callback = { viewModel.removePlaylist(playlist.id) }) {
                    PlainRowView(playlist.primaryText)
                }
            }
        }
    }
}