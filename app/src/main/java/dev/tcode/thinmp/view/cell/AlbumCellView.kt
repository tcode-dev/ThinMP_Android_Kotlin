package dev.tcode.thinmp.view.cell

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AlbumCellView(primaryText: String, secondaryText: String, uri: Uri) {
  Column(modifier = Modifier.padding(start = 8.dp)) {
    Text(primaryText)
    Text(secondaryText)
  }
}
