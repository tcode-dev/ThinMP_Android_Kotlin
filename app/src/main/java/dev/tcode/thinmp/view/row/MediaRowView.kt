package dev.tcode.thinmp.view.row

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.util.DividerView
import dev.tcode.thinmp.view.image.ImageView

@Composable
fun MediaRowView(
    primaryText: String,
    secondaryText: String,
    uri: Uri,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(start = StyleConstant.PADDING_LARGE.dp)) {
        Row(
            modifier = Modifier.padding(
                top = StyleConstant.PADDING_TINY.dp,
                bottom = StyleConstant.PADDING_TINY.dp
            )
        ) {
            ImageView(
                uri = uri,
                modifier = Modifier
                    .size(StyleConstant.IMAGE_SIZE.dp)
                    .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
            )
            Column(modifier = Modifier.padding(start = StyleConstant.PADDING_SMALL.dp)) {
                Text(primaryText, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    secondaryText,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        DividerView()
    }
}