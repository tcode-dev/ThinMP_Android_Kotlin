package dev.tcode.thinmp.view.topbar

import android.graphics.Insets
import android.view.WindowMetrics
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.R


@Composable
fun HeroTopbarView(title: String, visible: Boolean) {
    Box() {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(initialAlpha = 0.3F),
            exit = fadeOut(targetAlpha = 0.3F)
        ) {
//            Surface(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .statusBarsPadding()
//                    .height(50.dp)
////                    .blur(radius = 10.dp)
//                    .alpha(0.9F)
//            ) {
//            Image(
//                painter = painterResource(R.drawable.song_dark),
//                contentDescription = "",
//                contentScale = ContentScale.FillBounds,
//                modifier = Modifier
////                    .statusBarsPadding()
//                    .fillMaxWidth()
//                    .height(50.dp)
////                        .statusBarsPadding()
//                    .blur(10.dp)
//                    .alpha(0.9F)
//            )
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .statusBarsPadding()
//                        .height(50.dp)
//                ) {
//                }
//            }
        }
        if (visible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    title,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
