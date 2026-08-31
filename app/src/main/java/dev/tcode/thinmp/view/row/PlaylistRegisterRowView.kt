package dev.tcode.thinmp.view.row

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.util.DividerView

/**
 * A playlist row in the register popup. A playlist the song is already in cannot be tapped, so it
 * is greyed out and says so - otherwise it looks like a row that simply did nothing.
 */
@Composable
fun PlaylistRegisterRowView(text: String, registered: Boolean, modifier: Modifier = Modifier) {
    val color = if (registered) {
        MaterialTheme.colorScheme.primary.copy(alpha = StyleConstant.DISABLED_ALPHA)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(modifier = modifier.padding(start = StyleConstant.PADDING_LARGE.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(StyleConstant.ROW_HEIGHT.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                color = color,
                fontSize = StyleConstant.FONT_MEDIUM.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (registered) {
                Text(
                    stringResource(R.string.already_added),
                    color = color,
                    fontSize = StyleConstant.FONT_SMALL.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(start = StyleConstant.PADDING_SMALL.dp)
                )
            }
        }
        DividerView()
    }
}
