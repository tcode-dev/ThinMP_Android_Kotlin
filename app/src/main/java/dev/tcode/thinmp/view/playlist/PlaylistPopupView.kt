package dev.tcode.thinmp.view.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.PlaylistsViewModel

@Composable
fun PlaylistPopupView(songId: SongId, visiblePopup: MutableState<Boolean>, viewModel: PlaylistsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var isCreate by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    CustomLifecycleEventObserver(viewModel)

    Popup(
        alignment = Alignment.Center, onDismissRequest = { visiblePopup.value = false }, properties = PopupProperties(focusable = true)
    ) {
        Column(
            modifier = Modifier
                .padding(start = StyleConstant.PADDING_LARGE.dp, end = StyleConstant.PADDING_LARGE.dp)
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.secondary)
                .padding(top = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_LARGE.dp)
        ) {
            if (uiState.playlists.isNotEmpty() && !isCreate) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = StyleConstant.PADDING_SMALL.dp), horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(stringResource(R.string.create_playlist), Modifier.clickable {
                        isCreate = true
                    })
                    Text(stringResource(R.string.cancel), Modifier.clickable {
                        visiblePopup.value = false
                    })
                }
                Column {
                    uiState.playlists.forEach { playlist ->
                        PlainRowView(playlist.primaryText,
                            Modifier
                                .padding(end = StyleConstant.PADDING_LARGE.dp)
                                .clickable {
                                    viewModel.addPlaylist(playlist.id, songId)
                                    visiblePopup.value = false
                                })
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.playlist_name), textAlign = TextAlign.Center, modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = StyleConstant.PADDING_LARGE.dp)
                )
                OutlinedTextField(value = name, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = StyleConstant.PADDING_LARGE.dp, end = StyleConstant.PADDING_LARGE.dp), onValueChange = { name = it })
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = StyleConstant.PADDING_LARGE.dp), horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.createPlaylist(songId, name)
                            visiblePopup.value = false
                        },
                    ) {
                        Text(stringResource(R.string.done))
                    }
                    OutlinedButton(
                        onClick = {
                            isCreate = false
                        },
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}