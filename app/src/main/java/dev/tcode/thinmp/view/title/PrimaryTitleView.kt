package dev.tcode.thinmp.view.title

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun PrimaryTitleView(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(LocalConfiguration.current.screenWidthDp.dp - StyleConstant.BUTTON_SIZE.dp * 2 - StyleConstant.PADDING_TINY.dp * 2)
    )
}