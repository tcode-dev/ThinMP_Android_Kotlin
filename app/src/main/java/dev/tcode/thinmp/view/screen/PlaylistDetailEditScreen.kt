package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.swipe.SwipeToDismissView
import dev.tcode.thinmp.view.util.OnEvent
import dev.tcode.thinmp.viewModel.PlaylistDetailEditViewModel

@Composable
fun PlaylistDetailEditScreen(id: PlaylistId, viewModel: PlaylistDetailEditViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val doneCallback = { viewModel.save() }

    OnEvent(viewModel.saved) { navigator.backFromEvent() }

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(uiState.loaded, doneCallback) {
            item {
                OutlinedTextField(
                    value = uiState.primaryText,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StyleConstant.PADDING_LARGE.dp),
                    onValueChange = { viewModel.changeName(it) })
            }
            items(uiState.songs, key = { it.id }) { song ->
                SwipeToDismissView(callback = { viewModel.removeSong(song.songId) }) {
                    MediaRowView(song.name, song.artistName, song.getImageUri())
                }
            }
        }
    }
}