package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.PlayerViewModel

@ExperimentalFoundationApi
@Composable
fun PlayerScreen(
    navController: NavController, viewModel: PlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    Column(Modifier.fillMaxSize()) {
        val imageSize: Dp = LocalConfiguration.current.screenWidthDp.dp / 100 * 64
        val imageBottomPosition: Dp = LocalConfiguration.current.screenWidthDp.dp - imageSize - WindowInsets.systemBars.asPaddingValues().calculateTopPadding() - StyleConstant.ROW_HEIGHT.dp

        ConstraintLayout(
            Modifier
                .fillMaxWidth()
                .height(LocalConfiguration.current.screenWidthDp.dp)
        ) {
            val (primary) = createRefs()

            ImageView(
                uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp), painter = null
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
            Box(
                contentAlignment = Alignment.BottomCenter, modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = imageBottomPosition)
            ) {
                ImageView(
                    uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .statusBarsPadding()
                .size(StyleConstant.BUTTON_SIZE.dp)
                .clickable { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.round_arrow_back_ios_24), contentDescription = null, modifier = Modifier.size(StyleConstant.ICON_SIZE.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalConfiguration.current.screenWidthDp.dp), verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        uiState.primaryText,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        uiState.secondaryText,
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 30.dp, end = 30.dp)) {
                Slider(value = uiState.sliderPosition, onValueChange = { viewModel.seek(it) }, onValueChangeFinished = { viewModel.seekFinished() })
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(uiState.currentTime)
                    Text(uiState.durationTime)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { viewModel.prev() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_skip_previous_24), contentDescription = null, modifier = Modifier.size(72.dp)
                    )
                }
                if (uiState.isPlaying) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { viewModel.pause() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_pause_24), contentDescription = null, modifier = Modifier.size(88.dp)
                        )
                    }
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { viewModel.play() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_play_arrow_24), contentDescription = null, modifier = Modifier.size(88.dp)
                        )
                    }
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { viewModel.next() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_skip_next_24), contentDescription = null, modifier = Modifier.size(72.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_repeat_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_shuffle_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_person_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_favorite_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_playlist_add_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
            }
        }
    }
}
