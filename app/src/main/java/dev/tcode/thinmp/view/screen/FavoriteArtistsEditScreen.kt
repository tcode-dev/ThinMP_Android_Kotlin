package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.dropdownMenu.FavoriteArtistDropdownMenuItemView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.topAppBar.EditTopAppBarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopbarView
import dev.tcode.thinmp.view.util.miniPlayerHeight
import dev.tcode.thinmp.viewModel.FavoriteArtistsViewModel

@ExperimentalFoundationApi
@Composable
fun FavoriteArtistsEditScreen(viewModel: FavoriteArtistsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val miniPlayerHeight = miniPlayerHeight()
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        Box(Modifier.zIndex(3F)) {
            EditTopAppBarView(stringResource(R.string.edit), lazyListState.firstVisibleItemScrollOffset) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                    .clickable { }) {
                    Text(stringResource(R.string.done))
                }
            }
        }
        LazyColumn(state = lazyListState) {
            item {
                EmptyTopbarView()
            }
            itemsIndexed(uiState.artists) { index, artist ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    val expanded = remember { mutableStateOf(false) }
                    val close = { expanded.value = false }

                    PlainRowView(artist.name, Modifier.pointerInput(artist.id) {
                        detectTapGestures(onLongPress = { expanded.value = true }, onTap = { navigator.artistDetail(artist.id) })
                    })
                    DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), modifier = Modifier.background(MaterialTheme.colorScheme.onBackground), onDismissRequest = close) {
                        FavoriteArtistDropdownMenuItemView(artist.artistId, close)
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
            MiniPlayerView()
        }
    }
}