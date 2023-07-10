package dev.tcode.thinmp.view.collapsingTopAppBar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import dev.tcode.thinmp.view.topAppBar.PlainTopAppBarView
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopAppBarView

@Composable
fun GridCollapsingTopAppBarView(title: String, columns: GridCells, spanCount: Int, content: LazyGridScope.() -> Unit) {
    val lazyGridState = rememberLazyGridState()

    Box(Modifier.zIndex(1F)) {
        PlainTopAppBarView(title, visible = visibleTopAppBar(lazyGridState))
    }
    LazyVerticalGrid(columns = columns, state = lazyGridState) {
        item(span = { GridItemSpan(spanCount) }) {
            EmptyTopAppBarView()
        }
        content()
        item(span = { GridItemSpan(spanCount) }) {
            EmptyMiniPlayerView()
        }
    }
}

@Composable
private fun visibleTopAppBar(state: LazyGridState): Boolean {
    return state.firstVisibleItemScrollOffset > 1
}