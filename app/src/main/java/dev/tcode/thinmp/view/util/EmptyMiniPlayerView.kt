package dev.tcode.thinmp.view.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun EmptyMiniPlayerView() {
    Spacer(
        Modifier
            .navigationBarsPadding()
            .height(StyleConstant.ROW_HEIGHT.dp)
    )
}
