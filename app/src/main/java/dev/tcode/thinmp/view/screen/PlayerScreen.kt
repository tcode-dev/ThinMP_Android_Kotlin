package dev.tcode.thinmp.view.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.tcode.thinmp.view.util.isHeightMedium
import dev.tcode.thinmp.view.util.maxSize
import dev.tcode.thinmp.view.util.minSize
import dev.tcode.thinmp.view.util.systemBars
import dev.tcode.thinmp.viewModel.PlayerViewModel

@Composable
fun PlayerScreen(viewModel: PlayerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val visiblePopup = remember { mutableStateOf(false) }
    val minSize = minSize()
    val maxSize = maxSize()
    val isHeightMedium = isHeightMedium()
    val systemBars = systemBars()
    val edgeSize: Dp = minSize / 100 * 18
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (player, img, gradient) = createRefs()

        ImageView(
            uri = uiState.imageUri, contentScale = ContentScale.FillWidth, modifier = Modifier
                .fillMaxWidth()
                .blur(20.dp), painter = null
        )

        val gradientHeight = if (isLandscape) minSize else minSize / 2
        val brush = if (isLandscape) Brush.verticalGradient(
            0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.5F),
            1.0F to MaterialTheme.colorScheme.background.copy(alpha = 0.5F),
        ) else Brush.verticalGradient(
            0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0F),
            1.0F to MaterialTheme.colorScheme.background,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gradientHeight)
                .constrainAs(gradient) {
                    if (!isLandscape) {
                        top.linkTo(parent.top, margin = gradientHeight + 1.dp)
                    }
                }
                .background(brush = brush),
        ) {}
        if (!isLandscape || isHeightMedium) {
            val imageSize = if (isLandscape) minSize / 100 * 40 else minSize / 100 * 64
            val imageMargin = if (isLandscape) systemBars + StyleConstant.PADDING_LARGE.dp else edgeSize

            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier
                .constrainAs(img) {
                    centerHorizontallyTo(parent)
                    top.linkTo(parent.top, margin = imageMargin)
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

        val playerHeight = if (isPortrait) maxSize - minSize + (StyleConstant.PADDING_LARGE.dp * 2) + (edgeSize / 2)
        else if (isHeightMedium) minSize / 100 * 60 - systemBars - StyleConstant.PADDING_LARGE.dp
        else minSize
        val modifier = if (isPortrait) Modifier
            .fillMaxWidth()
            .height(playerHeight)
            .constrainAs(player) { top.linkTo(parent.bottom, margin = -(playerHeight)) }
        else if (isHeightMedium) Modifier
            .fillMaxWidth()
            .height(playerHeight)
            .padding(vertical = StyleConstant.PADDING_LARGE.dp)
            .constrainAs(player) { top.linkTo(parent.bottom, margin = -(playerHeight)) }
        else Modifier
            .fillMaxSize()
            .padding(vertical = StyleConstant.PADDING_LARGE.dp)

        Column(
            modifier = modifier, verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
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
    if (visiblePopup.value) {
        PlaylistRegisterPopupView(uiState.songId, { visiblePopup.value = false })
    }
}