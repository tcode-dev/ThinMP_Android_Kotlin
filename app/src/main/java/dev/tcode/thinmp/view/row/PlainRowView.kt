package dev.tcode.thinmp.view.row

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.util.DividerView

@Composable
fun PlainRowView(primaryText: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(start = StyleConstant.PADDING_LARGE.dp)) {
        Row(
            modifier = Modifier.height(StyleConstant.ROW_HEIGHT.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(primaryText, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.primary)
        }
        DividerView()
    }
}