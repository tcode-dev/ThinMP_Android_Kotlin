package dev.tcode.thinmp.view.popup

import androidx.compose.foundation.background
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
import dev.tcode.thinmp.register.PlaylistRegister
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.viewModel.PlaylistsViewModel

@Composable
fun PlaylistPopupView(songId: SongId, visiblePopup: MutableState<Boolean>, listener: PlaylistRegister, viewModel: PlaylistsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var text by remember { mutableStateOf("") }

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
            Text("playlist")
            OutlinedTextField(
                value = text,
                onValueChange = { text = it }
            )
            Row {
                OutlinedButton(
                    onClick = {
                        listener.createPlaylist(songId, text)

                        visiblePopup.value = false
                    },
                ) {
                    Text("ok")
                }
                OutlinedButton(
                    onClick = {
                        visiblePopup.value = false
                    },
                ) {
                    Text("cancel")
                }
            }
            Column {
                uiState.playlists.forEach { playlist ->
                    PlainRowView(playlist.name)
                }
            }
        }
    }
}