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
import com.google.accompanist.flowlayout.MainAxisAlignment
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
        val imageSize: Dp = LocalConfiguration.current.screenWidthDp.dp / 2

        ConstraintLayout(
            Modifier
                .fillMaxWidth()
                .height(LocalConfiguration.current.screenWidthDp.dp)
        ) {
            val (primary, secondary, tertiary, utilButton) = createRefs()

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
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
            ) {
                ImageView(
                    uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                )
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(LocalConfiguration.current.screenWidthDp.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            var sliderPosition by remember { mutableStateOf(0f) }
            Slider(value = sliderPosition, onValueChange = { sliderPosition = it }, modifier = Modifier.padding(start = 30.dp, end = 30.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .clickable { viewModel.next() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_skip_previous_24), contentDescription = null, modifier = Modifier.size(72.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .clickable { viewModel.next() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_play_arrow_24), contentDescription = null, modifier = Modifier.size(88.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .clickable { viewModel.next() }) {
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
                    .clickable { viewModel.next() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_repeat_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { viewModel.next() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_shuffle_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { viewModel.next() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_person_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { viewModel.next() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_favorite_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { viewModel.next() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_playlist_add_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                    )
                }
            }
        }
    }
}
