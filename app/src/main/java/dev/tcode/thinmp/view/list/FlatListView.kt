package dev.tcode.thinmp.view.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import dev.tcode.thinmp.model.Music
import dev.tcode.thinmp.model.SongModel
import dev.tcode.thinmp.view.listItem.FlatListItemView

@ExperimentalFoundationApi
@Composable
fun FlatListView(musics: List<Music>) {
    LazyColumn{
        items(musics) { music ->
            FlatListItemView(music.primaryText, music.secondaryText, music.getUri())
        }
    }
}
