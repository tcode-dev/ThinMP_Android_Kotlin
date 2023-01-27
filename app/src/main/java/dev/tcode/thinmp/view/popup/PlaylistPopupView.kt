package dev.tcode.thinmp.view.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.repository.realm.PlaylistRepository

@Composable
fun PlaylistPopupView(songId: SongId, visiblePopup: MutableState<Boolean>) {
    var text by remember { mutableStateOf("") }

    if (!visiblePopup.value) {
        return
    }

    Popup(alignment = Alignment.Center) {
        Column(modifier = Modifier
            .background(color = MaterialTheme.colors.secondary)
            .padding(StyleConstant.PADDING_LARGE.dp)) {
            Text("playlist")
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.padding(StyleConstant.PADDING_LARGE.dp)
            )
            Row {
                Button(
                    onClick = {
                        val repository = PlaylistRepository()

                        repository.create(songId, text)
                        visiblePopup.value = false
                    },
                ) {
                    Text("ok")
                }
                Button(
                    onClick = {
                        visiblePopup.value = false
                    },
                ) {
                    Text("cancel")
                }
            }
        }
    }
}