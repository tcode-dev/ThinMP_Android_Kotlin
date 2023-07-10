package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.swipe.SwipeToDismissView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.PlaylistDetailEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Composable
fun PlaylistDetailEditScreen(id: String, viewModel: PlaylistDetailEditViewModel = viewModel()) {
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
                OutlinedTextField(value = uiState.primaryText, singleLine = true, modifier = Modifier
                    .fillMaxWidth()
                    .padding(StyleConstant.PADDING_LARGE.dp), onValueChange = { viewModel.changeName(it) })
            }
            itemsIndexed(uiState.songs) { index, song ->
                SwipeToDismissView(callback = { viewModel.removeSong(index) }) {
                    MediaRowView(song.name, song.artistName, song.getImageUri())
                }
            }
        }
    }
}