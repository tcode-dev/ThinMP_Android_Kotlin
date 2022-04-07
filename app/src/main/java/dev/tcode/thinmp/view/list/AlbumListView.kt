package dev.tcode.thinmp.view.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.view.cell.AlbumCellView

@ExperimentalFoundationApi
@Composable
fun AlbumListView(albums: List<AlbumModel>) {
    LazyVerticalGrid(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        cells = GridCells.Fixed(2),
    ) {
        items(albums) { album ->
            AlbumCellView(album.name, album.artistName, album.getUri())
        }
    }
}