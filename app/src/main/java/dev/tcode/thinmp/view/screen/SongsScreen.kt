package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.collapsingTopAppBar.ColumnCollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.FavoriteSongDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistDropdownMenuItemView
import dev.tcode.thinmp.view.layout.CommonLayoutView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopAppBarView
import dev.tcode.thinmp.viewModel.SongsViewModel

@ExperimentalFoundationApi
@Composable
fun SongsScreen(viewModel: SongsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    CommonLayoutView { togglePopup, setPlaylistRegisterSongId ->
        ColumnCollapsingTopAppBarView(stringResource(R.string.songs)) {
            item {
                EmptyTopAppBarView()
            }
            itemsIndexed(uiState.songs) { index, song ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    val expanded = remember { mutableStateOf(false) }
                    val close = { expanded.value = false }
                    val closePlaylist = {
                        setPlaylistRegisterSongId(song.songId)
                        togglePopup()
                        close()
                    }

                    MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.pointerInput(index) {
                        detectTapGestures(onLongPress = { expanded.value = true }, onTap = { viewModel.start(index) })
                    })
                    DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), modifier = Modifier.background(MaterialTheme.colorScheme.onBackground), onDismissRequest = close) {
                        FavoriteSongDropdownMenuItemView(song.songId, close)
                        PlaylistDropdownMenuItemView(closePlaylist)
                    }
                }
            }
            item {
                EmptyMiniPlayerView()
            }
        }
    }
}