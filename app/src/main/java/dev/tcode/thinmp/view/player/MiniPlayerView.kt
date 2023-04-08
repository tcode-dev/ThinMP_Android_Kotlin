package dev.tcode.thinmp.view.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.NavConstant
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.MiniPlayerViewModel

@Composable
fun MiniPlayerView(navController: NavController, viewModel: MiniPlayerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    if (!uiState.isVisible) {
        return
    }

    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.secondary)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = StyleConstant.PADDING_LARGE.dp, top = StyleConstant.PADDING_TINY.dp, end = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_TINY.dp)
            .clickable { navController.navigate(NavConstant.PLAYER) }, verticalAlignment = Alignment.CenterVertically
    ) {
        ImageView(
            uri = uiState.imageUri, modifier = Modifier
                .size(StyleConstant.IMAGE_SIZE.dp)
                .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
        )
        Text(
            uiState.primaryText, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier
                .padding(start = StyleConstant.PADDING_SMALL.dp)
                .weight(1f)
        )
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .size(StyleConstant.BUTTON_SIZE.dp)
            .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
            .clickable { viewModel.toggle() }) {
            Icon(
                painter = painterResource(
                    id = if (uiState.isPlaying) {
                        R.drawable.round_pause_24
                    } else {
                        R.drawable.round_play_arrow_24
                    }
                ), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp)
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .size(StyleConstant.BUTTON_SIZE.dp)
            .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
            .clickable { viewModel.next() }) {
            Icon(painter = painterResource(id = R.drawable.round_skip_next_24), contentDescription = null, modifier = Modifier.size(StyleConstant.IMAGE_SIZE.dp))
        }
    }
}