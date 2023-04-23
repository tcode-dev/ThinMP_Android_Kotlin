package dev.tcode.thinmp.view.collapsingTopAppBar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

@Composable
fun CollapsingTopAppBarView(columns: GridCells, state: LazyGridState, content: LazyGridScope.() -> Unit) {
    LazyVerticalGrid(columns = columns, state = state, content = content)
}

@Composable
fun visibleTopAppBar(position: Int, state: LazyGridState): Boolean {
    if (state.firstVisibleItemIndex > 0) return true

    val density = LocalContext.current.resources.displayMetrics.density
    val width = LocalConfiguration.current.screenWidthDp
    val top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding().value

    return (state.firstVisibleItemScrollOffset / density) > (width - (top + position))
}