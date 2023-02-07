package dev.tcode.thinmp.view.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
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
        alignment = Alignment.Center,
        onDismissRequest = { visiblePopup.value = false },
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colors.secondary)
                .padding(StyleConstant.PADDING_LARGE.dp)
        ) {
            if (uiState.playlists.isNotEmpty() && !isCreate) {
                Row() {
                    Text("new playlist", Modifier.clickable {
                        isCreate = true
                    })
                    Text("cancel", Modifier.clickable {
                        visiblePopup.value = false
                    })
                }
                Column {
                    uiState.playlists.forEach { playlist ->
                        PlainRowView(playlist.name, Modifier.clickable {
                            viewModel.addPlaylist(playlist.id, songId)
                            visiblePopup.value = false
                        })
                    }
                }
            } else {
                Text("playlist name")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it }
                )
                Row {
                    OutlinedButton(
                        onClick = {
                            viewModel.createPlaylist(songId, name)
                            visiblePopup.value = false
                        },
                    ) {
                        Text("ok")
                    }
                    OutlinedButton(
                        onClick = {
                            isCreate = false
                        },
                    ) {
                        Text("cancel")
                    }
                }
            }
        }
    }
}