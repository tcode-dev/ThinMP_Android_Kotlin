package dev.tcode.thinmp.view.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.view.listItem.AlbumListItemView

@ExperimentalFoundationApi
@Composable
fun AlbumListView(navController: NavHostController, albums: List<AlbumModel>) {
    LazyVerticalGrid(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        cells = GridCells.Fixed(2),
    ) {
        items(albums) { album ->
            AlbumListItemView(navController, album.id, album.name, album.artistName, album.getUri())
        }
    }
}
