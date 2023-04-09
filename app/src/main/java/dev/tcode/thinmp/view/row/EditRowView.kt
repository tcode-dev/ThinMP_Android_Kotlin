package dev.tcode.thinmp.view.row

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.util.DividerView

@Composable
fun EditRowView(primaryText: String, checked: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(start = StyleConstant.PADDING_LARGE.dp)) {
        Row(
            modifier = Modifier.height(StyleConstant.ROW_HEIGHT.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = if (checked) R.drawable.check_box else R.drawable.check_box_outline_blank
                ), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(88.dp)
            )
            Text(primaryText, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DividerView()
    }
}