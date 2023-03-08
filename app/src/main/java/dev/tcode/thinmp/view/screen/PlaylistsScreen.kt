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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.topAppBar.PlainTopAppBarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopbarView
import dev.tcode.thinmp.viewModel.PlaylistsViewModel

@ExperimentalFoundationApi
@Composable
fun PlaylistsScreen(
    navController: NavController, viewModel: PlaylistsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val miniPlayerHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        Box(Modifier.zIndex(3F)) {
            PlainTopAppBarView(navController, stringResource(R.string.playlists), lazyListState.firstVisibleItemScrollOffset)
        }
        LazyColumn(state = lazyListState) {
            item {
                EmptyTopbarView()
            }
            itemsIndexed(uiState.playlists) { index, playlist ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    val expanded = remember { mutableStateOf(false) }
                    val close = { expanded.value = false }

                    PlainRowView(playlist.primaryText, Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = { expanded.value = true }, onTap = { navController.navigate(playlist.url) })
                    })
                    DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), onDismissRequest = close) {
                        DropdownMenuItem(onClick = {
                            viewModel.load(context)
                            expanded.value = false
                        }) {
                            Text(stringResource(R.string.remove_playlist))
                        }
                        ShortcutDropdownMenuItemView(playlist.id, close)
                    }
                }
            }
            item {
                EmptyMiniPlayerView()
            }
        }
        Box(modifier = Modifier.constrainAs(miniPlayer) {
            top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
        }) {
            MiniPlayerView(navController)
        }
    }
}