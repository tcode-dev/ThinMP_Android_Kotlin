package dev.tcode.thinmp.view.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun EmptyTopAppBarView() {
    Spacer(
        Modifier
            .statusBarsPadding()
            .height(StyleConstant.ROW_HEIGHT.dp)
    )
}