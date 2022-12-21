package dev.tcode.thinmp.view.image

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import dev.tcode.thinmp.R

@Composable
fun ImageView(
    uri: Uri,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    AsyncImage(
        model = uri,
        modifier = modifier,
        contentScale = contentScale,
        contentDescription = null,
        placeholder = painterResource(R.drawable.song_dark),
        error = painterResource(R.drawable.song_dark),
    )
}
