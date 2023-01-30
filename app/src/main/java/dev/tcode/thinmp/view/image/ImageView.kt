package dev.tcode.thinmp.view.image

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import dev.tcode.thinmp.R

@Composable
fun ImageView(
    uri: Uri, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit, painter: Painter? = painterResource(R.drawable.song_dark)
) {
    AsyncImage(
        model = uri,
        modifier = modifier,
        contentScale = contentScale,
        contentDescription = null,
        placeholder = painter,
        error = painter,
    )
}