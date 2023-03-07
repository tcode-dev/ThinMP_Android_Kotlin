package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.DividerView
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.spanSize
import dev.tcode.thinmp.viewModel.MainViewModel

@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val miniPlayerHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT
    val itemSize: Dp = spanSize()

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        LazyVerticalGrid(columns = GridCells.Fixed(StyleConstant.GRID_MAX_SPAN_COUNT)) {
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = StyleConstant.PADDING_LARGE.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = StyleConstant.PADDING_MEDIUM.dp)
                            .height(StyleConstant.ROW_HEIGHT.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.library), textAlign = TextAlign.Left, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, fontSize = 30.sp
                        )
                        Box(contentAlignment = Alignment.Center, modifier = Modifier
                            .size(StyleConstant.BUTTON_SIZE.dp)
                            .clickable { }) {
                            Icon(painter = painterResource(id = R.drawable.round_more_vert_24), contentDescription = null, modifier = Modifier.size(StyleConstant.ICON_SIZE.dp))
                        }
                    }
                    DividerView()
                }
            }
            items(items = uiState.menu, span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) { item ->
                PlainRowView(stringResource(item.id), modifier = Modifier.clickable {
                    navController.navigate(item.key)
                })
            }
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                Text(
                    stringResource(R.string.shortcut), fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.padding(
                        start = StyleConstant.PADDING_LARGE.dp, top = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_LARGE.dp
                    )
                )
            }
            itemsIndexed(items = uiState.shortcuts) { index, shortcut ->
                Box(
                    modifier = Modifier
                        .width(itemSize)
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    val expanded = remember { mutableStateOf(false) }
                    val close = { expanded.value = false }

                    GridCellView(index, StyleConstant.GRID_MAX_SPAN_COUNT, itemSize) {
                        AlbumCellView(shortcut.primaryText, shortcut.secondaryText, shortcut.imageUri, Modifier.pointerInput(Unit) {
                            detectTapGestures(onLongPress = { expanded.value = true }, onTap = { navController.navigate(shortcut.url) })
                        })
                    }
                    DropdownMenu(expanded = expanded.value, offset = DpOffset(0.dp, 0.dp), onDismissRequest = close) {
                        ShortcutDropdownMenuItemView(shortcut.itemId, close)
                    }
                }
            }
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                Text(
                    stringResource(R.string.recently_added), fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.padding(
                        start = StyleConstant.PADDING_LARGE.dp, top = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_LARGE.dp
                    )
                )
            }
            itemsIndexed(items = uiState.albums) { index, album ->
                Box(
                    modifier = Modifier
                        .width(itemSize)
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    val expanded = remember { mutableStateOf(false) }
                    val close = { expanded.value = false }

                    GridCellView(index, StyleConstant.GRID_MAX_SPAN_COUNT, itemSize) {
                        AlbumCellView(album.name, album.artistName, album.getImageUri(), Modifier.pointerInput(Unit) {
                            detectTapGestures(onLongPress = { expanded.value = true }, onTap = { navController.navigate("${NavConstant.ALBUM_DETAIL}/${album.id}") })
                        })
                    }
                    DropdownMenu(expanded = expanded.value, offset = DpOffset(0.dp, 0.dp), onDismissRequest = close) {
                        ShortcutDropdownMenuItemView(album.albumId, close)
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