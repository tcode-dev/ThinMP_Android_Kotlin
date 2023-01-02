package dev.tcode.thinmp.view.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.viewModel.MiniPlayerViewModel

@Composable
fun MiniPlayerView(viewModel: MiniPlayerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (!uiState.isVisible) {
        return
    }

    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colors.secondary)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, top = 5.dp, end = 20.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageView(
            uri = uiState.imageUri,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Text(
            uiState.primaryText,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
