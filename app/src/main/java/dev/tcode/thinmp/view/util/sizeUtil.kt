package dev.tcode.thinmp.view.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun spanSize(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp / StyleConstant.GRID_MAX_SPAN_COUNT
}

@Composable
fun miniPlayerHeight(): Float {
    return WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT
}