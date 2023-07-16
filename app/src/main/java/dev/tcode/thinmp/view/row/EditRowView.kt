package dev.tcode.thinmp.view.row

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.text.PlainTextView
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
            PlainTextView(primaryText)
        }
        DividerView()
    }
}