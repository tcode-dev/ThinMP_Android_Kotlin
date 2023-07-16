package dev.tcode.thinmp.view.cell

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.repository.realm.ItemType
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.text.PrimaryTextView
import dev.tcode.thinmp.view.text.SecondaryTextView

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
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = StyleConstant.PADDING_TINY.dp)
            ) {
                PrimaryTextView(primaryText)
            }
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryTextView(secondaryText)
            }
        }
    }
}