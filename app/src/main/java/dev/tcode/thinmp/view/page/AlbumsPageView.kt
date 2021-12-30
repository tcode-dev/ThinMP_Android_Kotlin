package dev.tcode.thinmp.view.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.tcode.thinmp.view.list.AlbumListView

@ExperimentalFoundationApi
@Composable
fun AlbumsPageView() {
    Column{
        Text(text = "Albums")
//        AlbumListView(names)
    }
}
