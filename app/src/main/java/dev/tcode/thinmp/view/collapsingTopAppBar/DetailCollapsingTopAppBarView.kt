package dev.tcode.thinmp.view.collapsingTopAppBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.tcode.thinmp.view.topAppBar.DetailTopAppBarView

@Composable
fun DetailCollapsingTopAppBarView(title: String, position: Int, columns: GridCells, dropdownMenus: @Composable ColumnScope.(callback: () -> Unit) -> Unit, content: LazyGridScope.() -> Unit) {
    val lazyGridState = rememberLazyGridState()

    Box(Modifier.zIndex(1F)) {
        val expanded = remember { mutableStateOf(false) }
        val callback = { expanded.value = !expanded.value }

        DetailTopAppBarView(title, visible = visibleTopAppBar(position, lazyGridState), callback)
        DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), modifier = Modifier.background(MaterialTheme.colorScheme.onBackground), onDismissRequest = callback) {
            dropdownMenus(callback = callback)
        }
    }
    LazyVerticalGrid(columns = columns, state = lazyGridState, content = content)
}

@Composable
private fun visibleTopAppBar(position: Int, state: LazyGridState): Boolean {
    if (state.firstVisibleItemIndex > 0) return true

    val density = LocalContext.current.resources.displayMetrics.density
    val width = LocalConfiguration.current.screenWidthDp
    val top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding().value

    return (state.firstVisibleItemScrollOffset / density) > (width - (top + position))
}