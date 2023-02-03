package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.popup.PlaylistPopupView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.topbar.ListTopbarView
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
    val miniPlayerHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT
    val lazyListState = rememberLazyListState()
    val visiblePopup = remember { mutableStateOf(false) }
    var playlistRegisterSongId = SongId("")

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        Box(Modifier.zIndex(3F)) {
            ListTopbarView(navController, "Songs", lazyListState.firstVisibleItemScrollOffset)
        }
        LazyColumn(state = lazyListState) {
            item {
                EmptyTopbarView()
            }
            itemsIndexed(uiState.songs) { index, song ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)) {
                    val expanded = remember { mutableStateOf(false) }

                    MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { expanded.value = true },
                            onTap = { viewModel.start(index) }
                        )
                    })
                    DropdownMenu(
                        expanded = expanded.value,
                        offset = DpOffset((-1).dp, 0.dp),
                        onDismissRequest = { expanded.value = false }) {
                        if (viewModel.existsFavorite(song.songId)) {
                            DropdownMenuItem(onClick = {
                                viewModel.deleteFavorite(song.songId)
                                expanded.value = false
                            }) {
                                Text("Remove from favorites")
                            }
                        } else {
                            DropdownMenuItem(onClick = {
                                viewModel.addFavorite(song.songId)
                                expanded.value = false
                            }) {
                                Text("Add to favorites")
                            }

                        }
                        DropdownMenuItem(onClick = {
                            playlistRegisterSongId = song.songId
                            visiblePopup.value = true
                            expanded.value = false
                        }) {
                            Text("Add to a playlist")
                        }
                    }
                }
            }
            item {
                EmptyMiniPlayerView()
            }
        }
        PlaylistPopupView(playlistRegisterSongId, visiblePopup, viewModel)
        Box(modifier = Modifier.constrainAs(miniPlayer) {
            top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
        }) {
            MiniPlayerView(navController)
        }
    }
}