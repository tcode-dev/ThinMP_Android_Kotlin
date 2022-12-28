package dev.tcode.thinmp.view.row

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.view.divider.DividerView

@Composable
fun PlainRowView(primaryText: String, modifier: Modifier = Modifier) {
    Column(modifier = Modifier.padding(start = 20.dp)) {
        Row(modifier = modifier.padding(10.dp)) {
            Text(primaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DividerView()
    }
}
