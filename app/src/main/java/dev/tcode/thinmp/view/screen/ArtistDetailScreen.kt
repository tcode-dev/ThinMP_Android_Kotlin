package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.model.media.valueObject.ArtistId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.collapsingTopAppBar.CollapsingTopAppBarView
import dev.tcode.thinmp.view.dropdownMenu.FavoriteArtistDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.FavoriteSongDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.playlist.PlaylistPopupView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.title.SectionTitleView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.view.util.miniPlayerHeight
import dev.tcode.thinmp.view.util.spanSize
import dev.tcode.thinmp.viewModel.ArtistDetailViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistDetailScreen(id: String, viewModel: ArtistDetailViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val visiblePopup = remember { mutableStateOf(false) }
    val itemSize: Dp = spanSize()
    val imageSize: Dp = LocalConfiguration.current.screenWidthDp.dp / 3
    val miniPlayerHeight = miniPlayerHeight()
    var playlistRegisterSongId = SongId("")
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        CollapsingTopAppBarView(
            title = uiState.primaryText,
            position = StyleConstant.COLLAPSING_TOP_APP_BAR_TITLE_POSITION,
            columns = GridCells.Fixed(StyleConstant.GRID_MAX_SPAN_COUNT),
            dropdownMenus = { callback ->
                FavoriteArtistDropdownMenuItemView(ArtistId(id), callback)
                ShortcutDropdownMenuItemView(ArtistId(id), callback)
            }) {
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                ConstraintLayout(
                    Modifier
                        .fillMaxWidth()
                        .height(LocalConfiguration.current.screenWidthDp.dp)
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
                            .height(200.dp)
                            .constrainAs(primary) { top.linkTo(parent.bottom, margin = (-200).dp) }
                            .background(
                                brush = Brush.verticalGradient(
                                    0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0F),
                                    1.0F to MaterialTheme.colorScheme.background,
                                )
                            ),
                    ) {}
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
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
                            .height(50.dp)
                            .constrainAs(secondary) {
                                top.linkTo(parent.bottom, margin = (-StyleConstant.COLLAPSING_TOP_APP_BAR_TITLE_POSITION).dp)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(uiState.primaryText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                            .constrainAs(tertiary) {
                                top.linkTo(parent.bottom, margin = (-55).dp)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(uiState.secondaryText, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            if (uiState.albums.isNotEmpty()) {
                item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                    SectionTitleView(stringResource(R.string.albums))
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
                            AlbumCellView(album.name, album.artistName, album.getImageUri(), Modifier.pointerInput(album.url) {
                                detectTapGestures(onLongPress = { expanded.value = true }, onTap = { navigator.albumDetail(album.id) })
                            })
                        }
                        DropdownMenu(expanded = expanded.value, offset = DpOffset(0.dp, 0.dp), modifier = Modifier.background(MaterialTheme.colorScheme.onBackground), onDismissRequest = close) {
                            ShortcutDropdownMenuItemView(album.albumId, close)
                        }
                    }
                }
            }
            if (uiState.songs.isNotEmpty()) {
                item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                    SectionTitleView(stringResource(R.string.songs))
                }
                itemsIndexed(items = uiState.songs, span = { _: Int, _: SongModel -> GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) { index, song ->
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
            }
            item {
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