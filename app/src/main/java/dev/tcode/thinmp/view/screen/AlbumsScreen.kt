package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.dropdownMenu.DropdownShortcutView
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.topAppBar.PlainTopAppBarView
import dev.tcode.thinmp.view.util.EmptyTopbarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.viewModel.AlbumsViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumsScreen(
    navController: NavController,
    viewModel: AlbumsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyGridState = rememberLazyGridState()
    val miniPlayerHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT
    val itemSize: Dp = LocalConfiguration.current.screenWidthDp.dp / StyleConstant.GRID_MAX_SPAN_COUNT

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        Box(Modifier.zIndex(3F)) {
            PlainTopAppBarView(navController, stringResource(R.string.albums), lazyGridState.firstVisibleItemScrollOffset)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(StyleConstant.GRID_MAX_SPAN_COUNT),
            state = lazyGridState
        ) {
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                EmptyTopbarView()
            }
            itemsIndexed(uiState.albums) { index, album ->
                val expanded = remember { mutableStateOf(false) }
                val close = { expanded.value = false }

                GridCellView(index, 2, itemSize) {
                    AlbumCellView(
                        album.name,
                        album.artistName,
                        album.getImageUri(),
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(onLongPress = { expanded.value = true }, onTap = { navController.navigate("${NavConstant.ALBUM_DETAIL}/${album.id}") })
                        }
                    )
                }
                DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), onDismissRequest = { expanded.value = false }) {
                    DropdownShortcutView(album.albumId, close)
                }
            }
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                EmptyMiniPlayerView()
            }
        }
        Box(modifier = Modifier
            .constrainAs(miniPlayer) {
                top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
            }) {
            MiniPlayerView(navController)
        }
    }
}