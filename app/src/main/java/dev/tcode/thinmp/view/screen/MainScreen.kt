package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.cell.ShortcutCellView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.layout.MiniPlayerLayoutView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.DropdownMenuView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.title.SectionTitleView
import dev.tcode.thinmp.view.util.CustomGridCellsFixed
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.DividerView
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.gridSpanCount
import dev.tcode.thinmp.viewModel.MainViewModel

@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val spanCount: Int = gridSpanCount()
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    MiniPlayerLayoutView {
        LazyVerticalGrid(columns = CustomGridCellsFixed(spanCount)) {
            item(span = { GridItemSpan(spanCount) }) {
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
                        val expanded = remember { mutableStateOf(false) }

                        Text(
                            stringResource(R.string.library),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Left,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            fontSize = StyleConstant.FONT_HUGE.sp
                        )
                        Box(contentAlignment = Alignment.Center, modifier = Modifier
                            .size(StyleConstant.BUTTON_SIZE.dp)
                            .clickable { expanded.value = !expanded.value }) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_more_vert_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(StyleConstant.ICON_SIZE.dp)
                            )
                            DropdownMenu(expanded = expanded.value,
                                offset = DpOffset((-1).dp, 0.dp),
                                modifier = Modifier.background(MaterialTheme.colorScheme.onBackground),
                                onDismissRequest = { expanded.value = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.edit), color = MaterialTheme.colorScheme.primary) }, onClick = {
                                    navigator.mainEdit()
                                })
                            }
                        }
                    }
                    DividerView()
                }
            }
            items(items = uiState.menu, span = { GridItemSpan(spanCount) }) { item ->
                if (item.visibility) {
                    PlainRowView(stringResource(item.id), modifier = Modifier.clickable {
                        navController.navigate(item.key)
                    })
                }
            }
            if (uiState.shortcutVisibility && uiState.shortcuts.isNotEmpty()) {
                item(span = { GridItemSpan(spanCount) }) {
                    SectionTitleView(stringResource(R.string.shortcut))
                }
                itemsIndexed(items = uiState.shortcuts) { index, shortcut ->
                    DropdownMenuView(dropdownContent = { callback ->
                        val callbackShortcut = {
                            callback()
                            viewModel.load()
                        }
                        ShortcutDropdownMenuItemView(shortcut.itemId, callbackShortcut)
                    }) { callback ->
                        GridCellView(index, spanCount) {
                            ShortcutCellView(shortcut.primaryText, shortcut.secondaryText, shortcut.imageUri, shortcut.type, Modifier.pointerInput(shortcut.url) {
                                detectTapGestures(onLongPress = { callback() }, onTap = { navController.navigate(shortcut.url) })
                            })
                        }
                    }
                }
            }
            if (uiState.recentlyAlbumsVisibility && uiState.albums.isNotEmpty()) {
                item(span = { GridItemSpan(spanCount) }) {
                    SectionTitleView(stringResource(R.string.recently_added))
                }
                itemsIndexed(items = uiState.albums) { index, album ->
                    DropdownMenuView(dropdownContent = { callback ->
                        val callbackAlbum = {
                            callback()
                            viewModel.load()
                        }
                        ShortcutDropdownMenuItemView(album.albumId, callbackAlbum)
                    }) { callback ->
                        GridCellView(index, spanCount) {
                            AlbumCellView(album.name, album.artistName, album.getImageUri(), Modifier.pointerInput(album.url) {
                                detectTapGestures(onLongPress = { callback() }, onTap = { navigator.albumDetail(album.id) })
                            })
                        }
                    }
                }
            }
            item(span = { GridItemSpan(spanCount) }) {
                EmptyMiniPlayerView()
            }
        }
    }
}