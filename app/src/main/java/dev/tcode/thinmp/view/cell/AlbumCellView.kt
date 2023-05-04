package dev.tcode.thinmp.view.cell

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.text.PrimaryTextView
import dev.tcode.thinmp.view.text.SecondaryTextView

@Composable
fun AlbumCellView(
    primaryText: String, secondaryText: String, uri: Uri, modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        val size = with(LocalDensity.current) { constraints.maxWidth.toDp() }

        Column(modifier = modifier) {
            Box(contentAlignment = Alignment.Center) {
                ImageView(
                    uri = uri, modifier = Modifier
                        .width(size)
                        .height(size)
                        .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                )
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