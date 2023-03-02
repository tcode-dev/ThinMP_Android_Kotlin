package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.valueObject.AlbumId
import dev.tcode.thinmp.model.media.valueObject.SongId
import dev.tcode.thinmp.view.dropdownMenu.FavoriteSongDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.PlaylistDropdownMenuItemView
import dev.tcode.thinmp.view.dropdownMenu.ShortcutDropdownMenuItemView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.playlist.PlaylistPopupView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.topAppBar.HeroTopAppBarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.viewModel.AlbumDetailViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumDetailScreen(
    navController: NavController, id: String, viewModel: AlbumDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val visiblePopup = remember { mutableStateOf(false) }
    val visibleHeroTopbarView = lazyListState.firstVisibleItemIndex > 0 || (lazyListState.firstVisibleItemScrollOffset / LocalContext.current.getResources()
        .getDisplayMetrics().density) > (LocalConfiguration.current.screenWidthDp - (WindowInsets.systemBars.asPaddingValues().calculateTopPadding().value + 90))
    val miniPlayerHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT
    var playlistRegisterSongId = SongId("")

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()

        Box(Modifier.zIndex(1F)) {
            val expanded = remember { mutableStateOf(false) }
            val toggle = { expanded.value = !expanded.value }

            HeroTopAppBarView(
                navController,
                uiState.primaryText,
                visible = visibleHeroTopbarView,
                toggle
            )
            DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), onDismissRequest = toggle) {
                ShortcutDropdownMenuItemView(AlbumId(id), toggle)
            }
        }
        LazyColumn(state = lazyListState) {
            item {
                ConstraintLayout(
                    Modifier
                        .fillMaxWidth()
                        .height(LocalConfiguration.current.screenWidthDp.dp)
                ) {
                    val (primary, secondary, tertiary) = createRefs()
                    ImageView(
                        uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .constrainAs(primary) {
                                top.linkTo(parent.bottom, margin = (-200).dp)
                            }
                            .background(
                                brush = Brush.verticalGradient(
                                    0.0f to MaterialTheme.colors.surface.copy(alpha = 0F),
                                    1.0F to MaterialTheme.colors.surface,
                                )
                            ),
                    ) {}
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .constrainAs(secondary) {
                                top.linkTo(parent.bottom, margin = (-90).dp)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            uiState.primaryText,
                            fontWeight = FontWeight.Bold,
                        )
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
                        Text(
                            uiState.secondaryText,
                        )
                    }
                }
            }
            itemsIndexed(uiState.songs) { index, song ->
                Box {
                    val expanded = remember { mutableStateOf(false) }
                    val closeFavorite = { expanded.value = false }
                    val closePlaylist = {
                        playlistRegisterSongId = song.songId
                        visiblePopup.value = true
                        expanded.value = false
                    }

                    MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = { expanded.value = true }, onTap = { viewModel.start(index) })
                    })
                    DropdownMenu(expanded = expanded.value, offset = DpOffset((-1).dp, 0.dp), onDismissRequest = { expanded.value = false }) {
                        FavoriteSongDropdownMenuItemView(song.songId, closeFavorite)
                        PlaylistDropdownMenuItemView(song.songId, closePlaylist)
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
            MiniPlayerView(navController)
        }
    }
}