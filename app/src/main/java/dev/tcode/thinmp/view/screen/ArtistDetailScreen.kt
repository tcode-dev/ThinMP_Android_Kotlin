package dev.tcode.thinmp.view.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.collapsingTopAppBar.DetailCollapsingTopAppBarView
import dev.tcode.thinmp.view.collapsingTopAppBar.detailSize
import dev.tcode.thinmp.view.dropdownMenu.FavoriteArtistDropdownMenuItemView
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
import dev.tcode.thinmp.view.title.SectionTitleView
import dev.tcode.thinmp.view.util.CustomGridCellsFixed
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.gridSpanCount
import dev.tcode.thinmp.view.util.isHeightMedium
import dev.tcode.thinmp.view.util.isLandscape
import dev.tcode.thinmp.viewModel.ArtistDetailViewModel

@Composable
fun ArtistDetailScreen(id: String, viewModel: ArtistDetailViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val spanCount: Int = gridSpanCount()
    val navigator = LocalNavigator.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val (size, gradientHeight, primaryTitlePosition, secondaryTitlePosition) = detailSize()
    val imageSize: Dp = if (isLandscape) size / 2 else size / 3

    CustomLifecycleEventObserver(viewModel)

    CommonLayoutView(uiState.isVisiblePlayer) { showPlaylistRegisterPopup ->
        DetailCollapsingTopAppBarView(title = uiState.primaryText, columns = CustomGridCellsFixed(spanCount), spanCount = spanCount, dropdownMenus = { callback ->
            FavoriteArtistDropdownMenuItemView(ArtistId(id), callback)
            ShortcutDropdownMenuItemView(ArtistId(id), callback)
        }) {
            item(span = { GridItemSpan(spanCount) }) {
                ConstraintLayout(
                    Modifier
                        .fillMaxWidth()
                        .height(size)
                ) {
                    val (primary, secondary, tertiary) = createRefs()

                    ImageView(
                        uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp), painter = null
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gradientHeight)
                            .constrainAs(primary) { top.linkTo(parent.bottom, margin = -gradientHeight) }
                            .background(
                                brush = Brush.verticalGradient(
                                    0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0F),
                                    1.0F to MaterialTheme.colorScheme.background,
                                )
                            ),
                    ) {}

                    val modifier = if (isLandscape() && !isHeightMedium()) Modifier.fillMaxSize().offset(y = -(size / 10)) else Modifier.fillMaxSize()

                    Box(
                        contentAlignment = Alignment.Center, modifier = modifier
                    ) {
                        ImageView(
                            uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier
                                .size(imageSize)
                                .clip(CircleShape)
                        )
                    }
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
            if (uiState.albums.isNotEmpty()) {
                item(span = { GridItemSpan(spanCount) }) {
                    SectionTitleView(stringResource(R.string.albums))
                }
                itemsIndexed(items = uiState.albums) { index, album ->
                    DropdownMenuView(dropdownContent = { callback ->
                        ShortcutDropdownMenuItemView(album.albumId, callback)
                    }) { callback ->
                        GridCellView(index, spanCount) {
                            AlbumCellView(album.name, album.artistName, album.getImageUri(), Modifier.pointerInput(album.url) {
                                detectTapGestures(onLongPress = { callback() }, onTap = { navigator.albumDetail(album.id) })
                            })
                        }
                    }
                }
            }
            if (uiState.songs.isNotEmpty()) {
                item(span = { GridItemSpan(spanCount) }) {
                    SectionTitleView(stringResource(R.string.songs))
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
}