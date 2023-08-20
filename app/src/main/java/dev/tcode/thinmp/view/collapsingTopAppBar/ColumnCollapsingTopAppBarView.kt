package dev.tcode.thinmp.view.collapsingTopAppBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import dev.tcode.thinmp.view.topAppBar.PlainTopAppBarView
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopAppBarView

@Composable
fun ColumnCollapsingTopAppBarView(title: String, content: LazyListScope.() -> Unit) {
    val lazyListState = rememberLazyListState()
    val scrollOffset = remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }

    Box(Modifier.zIndex(1F)) {
        PlainTopAppBarView(title, visible = scrollOffset.value > 1)
    }
    LazyColumn(state = lazyListState) {
        item {
            EmptyTopAppBarView()
        }
        content()
        item {
            EmptyMiniPlayerView()
        }
    }
}