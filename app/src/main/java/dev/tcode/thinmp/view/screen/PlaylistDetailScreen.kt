package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.view.collapsingTopAppBar.DetailCollapsingTopAppBarView
import dev.tcode.thinmp.view.collapsingTopAppBar.detailSize
import dev.tcode.thinmp.view.dropdownMenu.FavoriteSongDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.layout.CommonLayoutView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.DropdownMenuView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.title.PrimaryTitleView
import dev.tcode.thinmp.view.title.SecondaryTitleView
import dev.tcode.thinmp.view.util.CustomGridCellsFixed
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.gridSpanCount
import dev.tcode.thinmp.viewModel.PlaylistDetailViewModel

@Composable
fun PlaylistDetailScreen(id: String, viewModel: PlaylistDetailViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val spanCount: Int = gridSpanCount()
    val (size, gradientHeight, primaryTitlePosition, secondaryTitlePosition) = detailSize()

    CustomLifecycleEventObserver(viewModel)

    CommonLayoutView(uiState.isVisiblePlayer) { showPlaylistRegisterPopup ->
        DetailCollapsingTopAppBarView(title = uiState.primaryText, columns = CustomGridCellsFixed(spanCount), spanCount = spanCount, dropdownMenus = { callback ->
            DropdownMenuItem(text = { Text(stringResource(R.string.edit), color = MaterialTheme.colorScheme.primary) }, onClick = { navigator.playlistDetailEdit(id) })
            ShortcutDropdownMenuItemView(AlbumId(id), callback)
        }) {
            item(span = { GridItemSpan(spanCount) }) {
                ConstraintLayout(
                    Modifier
                        .fillMaxWidth()
                        .height(size)
                ) {
                    val (primary, secondary, tertiary) = createRefs()

                    ImageView(
                        uri = uiState.imageUri, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gradientHeight)
                            .constrainAs(primary) {
                                top.linkTo(parent.bottom, margin = -gradientHeight)
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0F),
                                    1.0F to MaterialTheme.colorScheme.background,
                                )
                            ),
                    ) {}
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(StyleConstant.ROW_HEIGHT.dp)
                            .constrainAs(secondary) {
                                top.linkTo(parent.top, margin = primaryTitlePosition)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        PrimaryTitleView(uiState.primaryText)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                            .constrainAs(tertiary) {
                                top.linkTo(parent.top, margin = secondaryTitlePosition)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        SecondaryTitleView(uiState.secondaryText)
                    }
                }
            }
            itemsIndexed(items = uiState.songs, span = { _: Int, _: SongModel -> GridItemSpan(spanCount) }) { index, song ->
                DropdownMenuView(dropdownContent = { callback ->
                    val callbackPlaylist = {
                        showPlaylistRegisterPopup(song.songId)
                        callback()
                    }
                    FavoriteSongDropdownMenuItemView(song.songId, callback)
                    PlaylistDropdownMenuItemView(callbackPlaylist)
                }) { callback ->
                    MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.pointerInput(index) {
                        detectTapGestures(onLongPress = { callback() }, onTap = { viewModel.start(index) })
                    })
                }
            }
        }
    }
}