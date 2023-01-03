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
import androidx.navigation.NavHostController
import dev.tcode.thinmp.model.media.SongModel
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.cell.GridCellView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.topbar.HeroTopbarView
import dev.tcode.thinmp.viewModel.ArtistDetailViewModel

const val MAX_SPAN_COUNT = 2

@ExperimentalFoundationApi
@Composable
fun ArtistDetailScreen(
    navController: NavHostController,
    id: String,
    viewModel: ArtistDetailViewModel = ArtistDetailViewModel(
        LocalContext.current,
        id
    )
) {
    val uiState = viewModel.uiState

    Box(Modifier.fillMaxWidth()) {
        val lazyGridState = rememberLazyGridState()
        val visibleHeroTopbarView =
            lazyGridState.firstVisibleItemIndex > 0 || (lazyGridState.firstVisibleItemScrollOffset / LocalContext.current.resources
                .displayMetrics.density) > (LocalConfiguration.current.screenWidthDp - (WindowInsets.systemBars.asPaddingValues()
                .calculateTopPadding().value + 90))

        Box(Modifier.zIndex(1F)) {
            HeroTopbarView(
                navController,
                uiState.primaryText,
                visible = visibleHeroTopbarView,
            )
        }
        LazyVerticalGrid(columns = GridCells.Fixed(MAX_SPAN_COUNT), state = lazyGridState) {
            item(span = { GridItemSpan(MAX_SPAN_COUNT) }) {
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
                            .blur(10.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ImageView(
                            uri = uiState.imageUri,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .size(120.dp)
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
            item(span = { GridItemSpan(MAX_SPAN_COUNT) }) {
                Text(
                    "Albums",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 20.dp, bottom = 15.dp)
                )
            }
            itemsIndexed(items = uiState.albums) { index, album ->
                val itemSize: Dp = (LocalConfiguration.current.screenWidthDp.dp / 2)

                GridCellView(index, MAX_SPAN_COUNT, itemSize) {
                    AlbumCellView(
                        navController,
                        album.id,
                        album.name,
                        album.artistName,
                        album.getUri()
                    )
                }
            }
            item(span = { GridItemSpan(MAX_SPAN_COUNT) }) {
                Text(
                    "Songs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 20.dp, bottom = 15.dp)
                )
            }
            itemsIndexed(
                items = uiState.songs,
                span = { _: Int, _: SongModel -> GridItemSpan(2) }) { index, song ->
                MediaRowView(song.name, song.artistName, song.getImageUri(), Modifier.clickable {
                    viewModel.start(index)
                })
            }
        }
    }
}
