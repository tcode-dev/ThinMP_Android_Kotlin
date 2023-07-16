package dev.tcode.thinmp.view.collapsingTopAppBar

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.topAppBar.DetailTopAppBarView
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.minSize

@Composable
fun DetailCollapsingTopAppBarView(title: String, columns: GridCells, spanCount: Int, dropdownMenus: @Composable ColumnScope.(callback: () -> Unit) -> Unit, content: LazyGridScope.() -> Unit) {
    val lazyGridState = rememberLazyGridState()

    Box(Modifier.zIndex(1F)) {
        val expanded = remember { mutableStateOf(false) }
        val callback = { expanded.value = !expanded.value }

        DetailTopAppBarView(title, visible = visibleTopAppBar(lazyGridState), callback)
        DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), modifier = Modifier.background(MaterialTheme.colorScheme.onBackground), onDismissRequest = callback) {
            dropdownMenus(callback = callback)
        }
    }
    LazyVerticalGrid(columns = columns, state = lazyGridState) {
        content()
        item(span = { GridItemSpan(spanCount) }) {
            EmptyMiniPlayerView()
        }
    }
}

data class DetailSize(
    val size: Dp,
    val gradientHeight: Dp,
    val primaryTitlePosition: Dp,
    val secondaryTitlePosition: Dp,
)

@Composable
fun detailSize(): DetailSize {
    return DetailSize(detailMinSize(), gradientHeight(), primaryTitlePosition(), secondaryTitlePosition())
}

@Composable
private fun detailMinSize(): Dp {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val size = minSize()

    return if (isLandscape) size - StyleConstant.ROW_HEIGHT.dp else size
}

@Composable
private fun gradientHeight(): Dp {
    return detailMinSize() / 2
}

@Composable
private fun primaryTitlePosition(): Dp {
    return (detailMinSize() / 5) * 4
}

@Composable
private fun secondaryTitlePosition(): Dp {
    return primaryTitlePosition() + StyleConstant.ROW_HEIGHT.dp - StyleConstant.PADDING_SMALL.dp
}

@Composable
private fun visibleTopAppBar(state: LazyGridState): Boolean {
    if (state.firstVisibleItemIndex > 0) return true

    val target = primaryTitlePosition() - WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val offset = (state.firstVisibleItemScrollOffset / LocalContext.current.resources.displayMetrics.density)

    return offset.dp > target
}