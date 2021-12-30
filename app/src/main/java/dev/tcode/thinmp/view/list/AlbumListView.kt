package dev.tcode.thinmp.view.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import dev.tcode.thinmp.view.cell.AlbumCellView

@ExperimentalFoundationApi
@Composable
fun AlbumListView(names: Array<String>) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(2)
    ) {
        items(names) { name ->
            AlbumCellView(name)
        }
    }
}