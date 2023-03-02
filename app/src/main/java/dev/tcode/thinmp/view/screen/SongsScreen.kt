package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.dropdownMenu.FavoriteSongDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistDropdownMenuItemView
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.playlist.PlaylistPopupView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.topAppBar.PlainTopAppBarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopbarView
import dev.tcode.thinmp.viewModel.SongsViewModel

@ExperimentalFoundationApi
@Composable
fun SongsScreen(
    navController: NavController, viewModel: SongsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val visiblePopup = remember { mutableStateOf(false) }
    val miniPlayerHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT
    var playlistRegisterSongId = SongId("")

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        Box(Modifier.zIndex(3F)) {
            PlainTopAppBarView(navController, stringResource(R.string.songs), lazyListState.firstVisibleItemScrollOffset)
        }
        LazyColumn(state = lazyListState) {
            item {
                EmptyTopbarView()
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
                        playlistRegisterSongId = song.songId
                        visiblePopup.value = true
                        close()
                    }

                    MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = { expanded.value = true }, onTap = { viewModel.start(index) })
                    })
                    DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), onDismissRequest = close) {
                        FavoriteSongDropdownMenuItemView(song.songId, close)
                        PlaylistDropdownMenuItemView(song.songId, closePlaylist)
                    }
                }
            }
            item {
                EmptyMiniPlayerView()
            }
        }
        if (visiblePopup.value) {
            PlaylistPopupView(playlistRegisterSongId, visiblePopup)
        }
        Box(modifier = Modifier.constrainAs(miniPlayer) {
            top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
        }) {
            MiniPlayerView(navController)
        }
    }
}