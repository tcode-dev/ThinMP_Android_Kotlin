package dev.tcode.thinmp.view.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import dev.tcode.thinmp.model.SongModel
import dev.tcode.thinmp.view.listItem.FlatListItemView

@ExperimentalFoundationApi
@Composable
fun FlatListView(songs: List<SongModel>) {
    LazyColumn{
        items(songs) { song ->
            FlatListItemView(song.name, song.artistName, song.getUri())
        }
    }
}
