package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.topbar.HeroTopbarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.viewModel.ArtistDetailViewModel

@ExperimentalFoundationApi
@Composable
fun ArtistDetailScreen(
    navController: NavController,
    id: String,
    viewModel: ArtistDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val lazyGridState = rememberLazyGridState()
        val visibleHeroTopbarView =
            lazyGridState.firstVisibleItemIndex > 0 || (lazyGridState.firstVisibleItemScrollOffset / LocalContext.current.resources
                .displayMetrics.density) > (LocalConfiguration.current.screenWidthDp - (WindowInsets.systemBars.asPaddingValues()
                .calculateTopPadding().value + 90))
        val (miniPlayer) = createRefs()
        val miniPlayerHeight =
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT
        val imageSize: Dp = LocalConfiguration.current.screenWidthDp.dp / 3

        Box(Modifier.zIndex(1F)) {
            HeroTopbarView(
                navController,
                uiState.primaryText,
                visible = visibleHeroTopbarView,
            )
        }
        LazyVerticalGrid(columns = GridCells.Fixed(StyleConstant.GRID_MAX_SPAN_COUNT), state = lazyGridState) {
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                ConstraintLayout(
                    Modifier
                        .fillMaxWidth()
                        .height(LocalConfiguration.current.screenWidthDp.dp)
                ) {
                    val (primary, secondary, tertiary) = createRefs()

                    ImageView(
                        uri = uiState.imageUri,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp),
                        painter = null
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ImageView(
                            uri = uiState.imageUri,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .size(imageSize)
                                .clip(CircleShape)
                        )
                    }
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
                    ) {
                    }
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
                        if (!visibleHeroTopbarView) {
                            Text(
                                uiState.primaryText,
                                fontWeight = FontWeight.Bold,
                            )
                        }
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
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                Text(
                    "Albums",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(
                        start = StyleConstant.PADDING_LARGE.dp,
                        bottom = StyleConstant.PADDING_MEDIUM.dp
                    )
                )
            }
            itemsIndexed(items = uiState.albums) { index, album ->
                val itemSize: Dp = LocalConfiguration.current.screenWidthDp.dp / StyleConstant.GRID_MAX_SPAN_COUNT

                GridCellView(index, StyleConstant.GRID_MAX_SPAN_COUNT, itemSize) {
                    AlbumCellView(
                        album.name,
                        album.artistName,
                        album.getImageUri(),
                        Modifier
                            .clickable {
                                navController.navigate("albumDetail/${album.id}")
                            }
                    )
                }
            }
            item(span = { GridItemSpan(StyleConstant.GRID_MAX_SPAN_COUNT) }) {
                Text(
                    "Songs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(
                        start = StyleConstant.PADDING_LARGE.dp,
                        bottom = StyleConstant.PADDING_MEDIUM.dp
                    )
                )
            }
            itemsIndexed(
                items = uiState.songs,
                span = { _: Int, _: SongModel -> GridItemSpan(2) }) { index, song ->
                MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.clickable {
                    viewModel.start(index)
                })
            }
            item {
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
