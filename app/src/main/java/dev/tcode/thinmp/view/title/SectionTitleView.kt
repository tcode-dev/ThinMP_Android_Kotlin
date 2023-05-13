package dev.tcode.thinmp.view.title

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun SectionTitleView(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = StyleConstant.FONT_LARGE.sp,
        modifier = Modifier.padding(start = StyleConstant.PADDING_LARGE.dp, top = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_LARGE.dp)
    )
}