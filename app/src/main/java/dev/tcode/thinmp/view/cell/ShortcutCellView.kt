package dev.tcode.thinmp.view.cell

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.repository.realm.ItemType
import dev.tcode.thinmp.view.image.ImageView

@Composable
fun ShortcutCellView(
    primaryText: String, secondaryText: String, uri: Uri, type: ItemType, modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        val size = with(LocalDensity.current) { constraints.maxWidth.toDp() }

        Column(modifier = modifier) {
            Box {
                if (type === ItemType.ARTIST) {
                    ImageView(
                        uri = uri, contentScale = ContentScale.FillWidth, modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                    )
                } else {
                    ImageView(
                        uri = uri, modifier = Modifier
                            .width(size)
                            .height(size)
                            .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                    )
                }
            }
            Text(
                primaryText,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = StyleConstant.PADDING_TINY.dp)
            )
            Text(
                secondaryText,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}