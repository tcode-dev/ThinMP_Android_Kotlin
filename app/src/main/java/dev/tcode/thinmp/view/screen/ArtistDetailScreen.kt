package dev.tcode.thinmp.view.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dev.tcode.thinmp.view.cell.AlbumCellView
import dev.tcode.thinmp.view.divider.DividerView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.topbar.HeroTopbarView
import dev.tcode.thinmp.viewModel.ArtistDetailViewModel

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
        val lazyListState = rememberLazyListState()
//        val visibleHeroTopbarView =
//            lazyListState.firstVisibleItemIndex > 0 || (lazyListState.firstVisibleItemScrollOffset / 2) > (LocalConfiguration.current.screenWidthDp - 46)
        val visibleHeroTopbarView =
            lazyListState.firstVisibleItemIndex > 0 || (lazyListState.firstVisibleItemScrollOffset / LocalContext.current.getResources().getDisplayMetrics().density) > (LocalConfiguration.current.screenWidthDp - 50)
//        var currentDp = lazyListState.firstVisibleItemScrollOffset / LocalContext.current.getResources().getDisplayMetrics().density
//        val screenWidthDp = LocalConfiguration.current.screenWidthDp
//        val visibleHeroTopbarView =
//            lazyListState.firstVisibleItemIndex > 0 || (lazyListState.firstVisibleItemScrollOffset / LocalContext.current.getResources().getDisplayMetrics().density) > LocalConfiguration.current.screenWidthDp
//        val visibleHeroTopbarView =
//            lazyListState.firstVisibleItemIndex > 0 || (lazyListState.firstVisibleItemScrollOffset) > LocalConfiguration.current.screenWidthDp
        Box(Modifier.zIndex(1F)) {
            HeroTopbarView(
                uiState.primaryText,
//                LocalContext.current.getResources().getDisplayMetrics().widthPixels.toString(),
//                lazyListState.firstVisibleItemScrollOffset.toString(),
                visible = visibleHeroTopbarView,
//            true
            )
        }
        LazyColumn(state = lazyListState) {
            item {
                ConstraintLayout(
                    Modifier
//                        .fillMaxWidth()
                        .width(LocalConfiguration.current.screenWidthDp.dp)
                        .height(LocalConfiguration.current.screenWidthDp.dp)
                ) {
                    val (primary, secondary, tertiary) = createRefs()
                    ImageView(
                        uri = uiState.imgUri,
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
                            uri = uiState.imgUri,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    }
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp)
//                            .constrainAs(primary) {
//                                top.linkTo(parent.top, margin = 200.dp)
//                            }
//                            .background(
//                                brush = Brush.verticalGradient(
//                                    0.0f to MaterialTheme.colors.surface.copy(alpha = 0F),
//                                    1.0F to MaterialTheme.colors.surface,
//                                )
//                            ),
//                    ) {
//                    }
                    Row(
                        Modifier
//                            .fillMaxWidth()
                            .height(50.dp)
                            .constrainAs(secondary) {
                                top.linkTo(parent.bottom, margin = (-50).dp)
                            }
                            .background(
                                color = Color(0xAAAAAA00)
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,

                    ) {
                        AnimatedVisibility(
                            visible = !visibleHeroTopbarView,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {

                        }
//                        Text(
//                            uiState.primaryText,
//                            fontWeight = FontWeight.Bold,
//                        )
                        Text(
                            uiState.primaryText,
//                            "$currentDp : $screenWidthDp" ,
                            modifier = Modifier.background(
                                color = Color(0xFFFF0000)
                            )
                        )
                    }
//                    Row(
//                        Modifier
//                            .fillMaxWidth()
//                            .height(25.dp)
//                            .constrainAs(tertiary) {
//                                top.linkTo(parent.bottom, margin = (-55).dp)
//                            },
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center,
//                    ) {
//                        Text(
//                            uiState.secondaryText,
//                        )
//                    }
                }
            }
            item {
                val itemSize: Dp = (LocalConfiguration.current.screenWidthDp.dp / 2)
                FlowRow(
                    mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween
                ) {
                    for (album in uiState.albums) {
                        Box(modifier = Modifier.width(itemSize)) {
                            AlbumCellView(
                                navController,
                                album.id,
                                album.name,
                                album.artistName,
                                album.getUri()
                            )
                        }
                    }
                }
            }
            items(uiState.songs) { song ->
                MediaRowView(song.name, song.artistName, song.getUri())
                DividerView()
            }
        }
    }
}
