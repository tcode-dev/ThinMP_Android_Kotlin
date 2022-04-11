package dev.tcode.thinmp.view.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.model.Music
import dev.tcode.thinmp.view.listItem.GridListItemView

@ExperimentalFoundationApi
@Composable
fun GridListView(musics: List<Music>) {
    LazyVerticalGrid(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        cells = GridCells.Fixed(2),
    ) {
        items(musics) { music ->
            GridListItemView(music.primaryText, music.secondaryText, music.getUri())
        }
    }
}
