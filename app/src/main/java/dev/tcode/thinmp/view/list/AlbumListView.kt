package dev.tcode.thinmp.view.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.view.cell.AlbumCellView

@ExperimentalFoundationApi
@Composable
fun AlbumListView(albums: List<AlbumModel>) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(2)
    ) {
        items(albums) { album ->
            AlbumCellView(album.name, album.artistName, album.getUri())
        }
    }
}