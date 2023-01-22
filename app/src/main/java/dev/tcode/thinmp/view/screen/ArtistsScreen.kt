package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.topbar.ListTopbarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.EmptyTopbarView
import dev.tcode.thinmp.viewModel.ArtistsViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistsScreen(
    navController: NavController, viewModel: ArtistsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val lazyListState = rememberLazyListState()
        val (miniPlayer) = createRefs()
        val miniPlayerHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT

        Box(Modifier.zIndex(3F)) {
            ListTopbarView(navController, "Artists", lazyListState.firstVisibleItemScrollOffset)
        }
        LazyColumn(state = lazyListState) {
            item {
                EmptyTopbarView()
            }
            items(uiState.artists) { artist ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)) {
                    val expanded = remember { mutableStateOf(false) }

                    PlainRowView(artist.name, Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = { expanded.value = true }, onTap = { navController.navigate("artistDetail/${artist.id}") })
                    })
                    DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), onDismissRequest = { expanded.value = false }) {
                        if (viewModel.existsFavorite(artist.artistId)) {
                            DropdownMenuItem(onClick = {
                                viewModel.deleteFavorite(artist.artistId)
                                expanded.value = false
                            }) {
                                Text("Remove from favorites")
                            }
                        } else {
                            DropdownMenuItem(onClick = {
                                viewModel.addFavorite(artist.artistId)
                                expanded.value = false
                            }) {
                                Text("Add to favorites")
                            }
                        }
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