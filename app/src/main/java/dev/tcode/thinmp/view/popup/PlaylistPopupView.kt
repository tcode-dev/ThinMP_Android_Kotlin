package dev.tcode.thinmp.view.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup

@Composable
fun PlaylistPopupView() {
    Popup(alignment = Alignment.Center) {
        Box(modifier = Modifier.background(color = MaterialTheme.colors.secondary)) {
            Text("playlist")
        }
    }
}