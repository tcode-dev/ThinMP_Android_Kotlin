package dev.tcode.thinmp.view.row

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PlainRowView(primaryText: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(10.dp)) {
        Text(primaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
