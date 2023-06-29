package dev.tcode.thinmp.view.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.collapsingTopAppBar.DetailCollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.FavoriteSongDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.playlist.PlaylistPopupView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.title.PrimaryTitleView
import dev.tcode.thinmp.view.title.SecondaryTitleView
import dev.tcode.thinmp.view.util.CustomGridCellsFixed
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.gridSpanCount
import dev.tcode.thinmp.view.util.miniPlayerHeight
import dev.tcode.thinmp.viewModel.AlbumDetailViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumDetailScreen(id: String, viewModel: AlbumDetailViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val visiblePopup = remember { mutableStateOf(false) }
    val miniPlayerHeight = miniPlayerHeight()
    var playlistRegisterSongId = SongId("")
    val spanCount: Int = gridSpanCount()
    val width = LocalConfiguration.current.screenWidthDp.dp
    val height = LocalConfiguration.current.screenHeightDp.dp + WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val minSize = if (isLandscape) min(width, height) - StyleConstant.ROW_HEIGHT.dp else min(width, height)
    val primaryTitlePosition = -(minSize / 3)
    val secondaryTitlePosition = primaryTitlePosition + StyleConstant.ROW_HEIGHT.dp
    val gradientHeight = minSize / 2

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        DetailCollapsingTopAppBarView(title = uiState.primaryText,
            position = StyleConstant.COLLAPSING_TOP_APP_BAR_TITLE_POSITION,
            columns = CustomGridCellsFixed(spanCount),
            dropdownMenus = { callback ->
                ShortcutDropdownMenuItemView(AlbumId(id), callback)
            }) {
            item(span = { GridItemSpan(spanCount) }) {
                ConstraintLayout(
                    Modifier
                        .fillMaxWidth()
                        .height(minSize)
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
                                top.linkTo(parent.bottom, margin = (-gradientHeight))
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
                                top.linkTo(parent.bottom, margin = primaryTitlePosition)
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
                                top.linkTo(parent.bottom, margin = secondaryTitlePosition)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        SecondaryTitleView(uiState.secondaryText)
                    }
                }
            }
            itemsIndexed(uiState.songs, span = { _: Int, _: SongModel -> GridItemSpan(spanCount) }) { index, song ->
                Box {
                    val expanded = remember { mutableStateOf(false) }
                    val closeFavorite = { expanded.value = false }
                    val closePlaylist = {
                        playlistRegisterSongId = song.songId
                        visiblePopup.value = true
                        expanded.value = false
                    }

                    MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.pointerInput(index) {
                        detectTapGestures(onLongPress = { expanded.value = true }, onTap = { viewModel.start(index) })
                    })
                    DropdownMenu(expanded = expanded.value,
                        offset = DpOffset((-1).dp, 0.dp),
                        modifier = Modifier.background(MaterialTheme.colorScheme.onBackground),
                        onDismissRequest = { expanded.value = false }) {
                        FavoriteSongDropdownMenuItemView(song.songId, closeFavorite)
                        PlaylistDropdownMenuItemView(song.songId, closePlaylist)
                    }
                }
            }
            item(span = { GridItemSpan(spanCount) }) {
                EmptyMiniPlayerView()
            }
        }
        if (visiblePopup.value) {
            PlaylistPopupView(playlistRegisterSongId, visiblePopup)
        }
        Box(modifier = Modifier.constrainAs(miniPlayer) {
            top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
        }) {
            MiniPlayerView()
        }
    }
}