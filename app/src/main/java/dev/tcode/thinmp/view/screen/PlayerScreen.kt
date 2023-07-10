package dev.tcode.thinmp.view.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.config.RepeatState
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.button.BackButtonView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.playlist.PlaylistRegisterPopupView
import dev.tcode.thinmp.view.title.PrimaryTitleView
import dev.tcode.thinmp.view.title.SecondaryTitleView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.maxSize
import dev.tcode.thinmp.view.util.minSize
import dev.tcode.thinmp.viewModel.PlayerViewModel

@ExperimentalFoundationApi
@Composable
fun PlayerScreen(viewModel: PlayerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val visiblePopup = remember { mutableStateOf(false) }
    val minSize = minSize()
    val maxSize = maxSize()
    val imageSize: Dp = minSize / 100 * 64
    val edgeSize: Dp = minSize / 100 * 18
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val gradientHeight = if (isLandscape) minSize else minSize / 2
    val playerHeight = if (isLandscape) minSize else maxSize - minSize + (StyleConstant.PADDING_LARGE.dp * 2) + (edgeSize / 2)

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (player, img, gradient) = createRefs()

        ImageView(
            uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier
                .fillMaxWidth()
                .blur(20.dp), painter = null
        )
        val gradientModifier = if (isLandscape) Modifier.statusBarsPadding() else Modifier
        Box(
            modifier = gradientModifier
                .fillMaxWidth()
                .height(gradientHeight)
                .constrainAs(gradient) {
                    if (!isLandscape) {
                        top.linkTo(parent.top, margin = gradientHeight)
                    }
                }
                .background(
                    brush = Brush.verticalGradient(
                        0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0F),
                        1.0F to MaterialTheme.colorScheme.background,
                    )
                ),
        ) {}
        if (!isLandscape) {
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier
                .constrainAs(img) {
                    centerHorizontallyTo(parent)
                    top.linkTo(parent.top, margin = edgeSize)
                }
                .size(imageSize)) {
                ImageView(
                    uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
        BackButtonView(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = StyleConstant.PADDING_TINY.dp)
        )
        val playerModifier = if (isLandscape) Modifier
            .fillMaxSize()
            .padding(vertical = StyleConstant.PADDING_LARGE.dp) else Modifier
            .fillMaxWidth()
            .height(playerHeight)
            .constrainAs(player) { top.linkTo(parent.bottom, margin = -(playerHeight)) }
        Box(
            contentAlignment = Alignment.TopCenter, modifier = playerModifier
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StyleConstant.PADDING_LARGE.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    PrimaryTitleView(uiState.primaryText)
                    SecondaryTitleView(uiState.secondaryText)
                }
                Column(
                    modifier = Modifier.padding(horizontal = 30.dp)
                ) {
                    Slider(value = uiState.sliderPosition,
                        colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary),
                        onValueChange = { viewModel.seek(it) },
                        onValueChangeFinished = { viewModel.seekFinished() })
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = StyleConstant.PADDING_SMALL.dp), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(uiState.currentTime, color = MaterialTheme.colorScheme.secondary)
                        Text(uiState.durationTime, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                        .clickable { viewModel.prev() }) {
                        Icon(painter = painterResource(id = R.drawable.round_skip_previous_24), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                        .clickable { viewModel.toggle() }) {
                        Icon(
                            painter = painterResource(id = if (uiState.isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(88.dp)
                        )
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                        .clickable { viewModel.next() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_skip_next_24), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(StyleConstant.BUTTON_SIZE.dp)
                            .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                            .clickable { viewModel.changeRepeat() }) {
                        Icon(
                            painter = painterResource(id = if (uiState.repeat == RepeatState.ONE) R.drawable.round_repeat_one_24 else R.drawable.round_repeat_24),
                            contentDescription = null,
                            modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp),
                            tint = if (uiState.repeat == RepeatState.OFF) MaterialTheme.colorScheme.primary.copy(alpha = 0.38f) else MaterialTheme.colorScheme.primary,
                        )
                    }
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(StyleConstant.BUTTON_SIZE.dp)
                            .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                            .clickable { viewModel.changeShuffle() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_shuffle_24),
                            contentDescription = null,
                            tint = if (uiState.shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                            modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                        )
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .size(StyleConstant.BUTTON_SIZE.dp)
                        .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                        .clickable {
                            viewModel.favoriteArtist()
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_person_24),
                            contentDescription = null,
                            tint = if (uiState.isFavoriteArtist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                            modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                        )
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .size(StyleConstant.BUTTON_SIZE.dp)
                        .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                        .clickable {
                            viewModel.favoriteSong()
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_favorite_24),
                            contentDescription = null,
                            tint = if (uiState.isFavoriteSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                            modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                        )
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .size(StyleConstant.BUTTON_SIZE.dp)
                        .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                        .clickable {
                            visiblePopup.value = true
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_playlist_add_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
                        )
                    }
                }
            }
        }
    }
    if (visiblePopup.value) {
        PlaylistRegisterPopupView(uiState.songId, { visiblePopup.value = false })
    }
}