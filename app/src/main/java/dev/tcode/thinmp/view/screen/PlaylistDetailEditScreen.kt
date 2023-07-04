package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopAppBarView
import dev.tcode.thinmp.viewModel.PlaylistDetailEditViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@ExperimentalFoundationApi
@Composable
fun PlaylistDetailEditScreen(id: String, viewModel: PlaylistDetailEditViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    var name by remember { mutableStateOf(uiState.primaryText) }
    val doneCallback = {
        viewModel.updateName(PlaylistId(id), name)
        navigator.back()
    }

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(doneCallback) {
            item {
                EmptyTopAppBarView()
            }
            item {
                OutlinedTextField(value = name, singleLine = true, modifier = Modifier
                    .fillMaxWidth()
                    .padding(StyleConstant.PADDING_LARGE.dp), onValueChange = { name = it })
            }
            itemsIndexed(uiState.songs) { index, song ->
                val dismissState = rememberDismissState(confirmStateChange = {
                    if (it == DismissValue.DismissedToStart) {
                        println("dismissState: DismissedToStart")
                    }
                    true
                })
                SwipeToDismiss(state = dismissState, directions = setOf(DismissDirection.EndToStart), background = {

                }) {
                    MediaRowView(song.name, song.artistName, song.getImageUri())
                }
            }
            item {
                EmptyMiniPlayerView()
            }
        }
    }
}