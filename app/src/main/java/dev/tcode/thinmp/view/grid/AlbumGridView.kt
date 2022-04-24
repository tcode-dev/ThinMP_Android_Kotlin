package dev.tcode.thinmp.view.grid

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.tcode.thinmp.model.AlbumModel
import dev.tcode.thinmp.view.cell.AlbumCellView

@ExperimentalFoundationApi
@Composable
fun AlbumGridView(
    navController: NavHostController,
    albums: List<AlbumModel>,
    lazyGridState: LazyGridState
) {
    LazyVerticalGrid(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        columns = GridCells.Fixed(2),
        state = lazyGridState
    ) {
        item {
            SpacerView()
        }
        item {
            SpacerView()
        }
        items(albums) { album ->
            AlbumCellView(navController, album.id, album.name, album.artistName, album.getUri())
        }
    }
}

// TODO: 暫定でtopbarの下に余白を追加しているので削除
@Composable
fun SpacerView() {
    Spacer(
        Modifier
            .statusBarsPadding()
            .height(50.dp)
    )
}
