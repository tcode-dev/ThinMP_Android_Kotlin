package dev.tcode.thinmp.view.player

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.viewModel.MiniPlayerViewModel

@Composable
fun MiniPlayerView(viewModel: MiniPlayerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

//    if (!uiState.isVisible) {
//        Log.d("MiniPlayerView", "false")
//        return
//    }
    Log.d("MiniPlayerView", uiState.primaryText)
    Row(modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)) {
        ImageView(
            uri = uiState.imageUri,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    Log.d("MiniPlayerView", "clicked")
                    viewModel.forceUpdate()
                }
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
