package dev.tcode.thinmp.view.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.max
import dev.tcode.thinmp.constant.StyleConstant
import java.lang.Integer.max

@Composable
fun gridSpanCount(): Int {
    val spanCount = LocalConfiguration.current.screenWidthDp / StyleConstant.GRID_SPAN_BASE_SIZE

    return max(spanCount, StyleConstant.GRID_MIN_SPAN_COUNT)
}

@Composable
fun miniPlayerHeight(): Float {
    return WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT
}

@Composable
fun screenWidth(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp
}

@Composable
fun screenHeight(): Dp {
    return LocalConfiguration.current.screenHeightDp.dp + WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
}

@Composable
fun minSize(): Dp {
    return min(screenWidth(), screenHeight())
}

@Composable
fun maxSize(): Dp {
    return max(screenWidth(), screenHeight())
}