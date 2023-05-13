package dev.tcode.thinmp.view.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun SecondaryTextView(text: String) {
    Text(text, color = MaterialTheme.colorScheme.secondary, fontSize = StyleConstant.FONT_TINY.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}