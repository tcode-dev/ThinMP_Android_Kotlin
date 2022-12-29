package dev.tcode.thinmp.view.grid

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.tcode.thinmp.model.media.AlbumModel
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.util.EmptyView

@ExperimentalFoundationApi
@Composable
fun AlbumGridView(
    navController: NavHostController,
    albums: List<AlbumModel>,
    lazyGridState: LazyGridState,
    itemSize: Dp
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = lazyGridState
    ) {
        item {
            EmptyView()
        }
        item {
            EmptyView()
        }
        itemsIndexed(albums) { index, album ->
            val even = (index % 2) == 0
            val start = if (even) 20.dp else 10.dp
            val end = if (even) 10.dp else 20.dp
            Box(
                modifier = Modifier
                    .width(itemSize)
                    .padding(
                        start = start,
                        end = end,
                        top = 0.dp,
                        bottom = 20.dp
                    )
            ) {
                AlbumCellView(
                    navController,
                    album.id,
                    album.name,
                    album.artistName,
                    album.getUri()
                )
            }
        }
    }
}
