package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.valueObject.PlaylistId
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyTopAppBarView
import dev.tcode.thinmp.viewModel.PlaylistDetailEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
                Text(
                    text = stringResource(R.string.playlist_name),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = StyleConstant.PADDING_LARGE.dp)
                )
            }
            item {
                OutlinedTextField(value = name,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = StyleConstant.PADDING_LARGE.dp, end = StyleConstant.PADDING_LARGE.dp),
                    onValueChange = { name = it })
            }
        }
    }
}