package dev.tcode.thinmp.view.listItem

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.view.image.ImageView

@Composable
fun FlatListItemView(primaryText: String, secondaryText: String?, uri: Uri?) {
    Row(modifier = Modifier.padding(10.dp)) {
        ImageView(uri = uri, size = 44.dp)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(primaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (secondaryText != null) {
                Text(secondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
