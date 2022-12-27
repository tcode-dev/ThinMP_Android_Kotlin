package dev.tcode.thinmp.view.row

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tcode.thinmp.view.divider.DividerView
import dev.tcode.thinmp.view.image.ImageView

@Composable
fun MediaRowView(primaryText: String, secondaryText: String, uri: Uri) {
    Column(modifier = Modifier.padding(start = 20.dp)) {
        Row(modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
            ImageView(uri = uri, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(4.dp)))
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(primaryText, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(secondaryText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        DividerView()
    }
}
