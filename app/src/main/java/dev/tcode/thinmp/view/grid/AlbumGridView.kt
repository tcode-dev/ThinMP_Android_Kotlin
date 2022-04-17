package dev.tcode.thinmp.view.grid

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.view.cell.AlbumListItemView

@ExperimentalFoundationApi
@Composable
fun AlbumGridView(navController: NavHostController, albums: List<AlbumModel>) {
    LazyVerticalGrid(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        columns = GridCells.Fixed(2),
    ) {
        items(albums) { album ->
            AlbumListItemView(navController, album.id, album.name, album.artistName, album.getUri())
        }
    }
}
