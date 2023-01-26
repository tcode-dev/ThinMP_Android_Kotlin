package dev.tcode.thinmp.view.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup

@Composable
fun PlaylistPopupView(visiblePopup: MutableState<Boolean>) {
    if (!visiblePopup.value) {
        return
    }

    Popup(alignment = Alignment.Center) {
        Column(modifier = Modifier.background(color = MaterialTheme.colors.secondary)) {
            Text("playlist")
            Text("cancel", modifier = Modifier.clickable {
                visiblePopup.value = false
            })
        }
    }
}