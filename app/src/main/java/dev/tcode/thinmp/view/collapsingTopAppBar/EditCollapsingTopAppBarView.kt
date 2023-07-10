package dev.tcode.thinmp.view.collapsingTopAppBar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import dev.tcode.thinmp.view.topAppBar.EditTopAppBarView
import dev.tcode.thinmp.view.util.EmptyTopAppBarView

@Composable
fun EditCollapsingTopAppBarView(callback: () -> Unit, content: LazyListScope.() -> Unit) {
    val lazyListState = rememberLazyListState()

    Box(Modifier.zIndex(1F)) {
        EditTopAppBarView(visible = visibleTopAppBar(lazyListState), callback)
    }
    LazyColumn(state = lazyListState) {
        item {
            EmptyTopAppBarView()
        }
        content()
    }
}

@Composable
private fun visibleTopAppBar(state: LazyListState): Boolean {
    return state.firstVisibleItemScrollOffset > 1
}