package dev.tcode.thinmp.view.image

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import dev.tcode.thinmp.R
import java.io.IOException

@Composable
fun ImageView(uri: Uri, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Fit,) {
    var bitmap: Bitmap? = null
    val source = ImageDecoder.createSource(LocalContext.current.contentResolver, uri)

    try {
        bitmap = ImageDecoder.decodeBitmap(source)
    } catch (e: IOException) {
        // handle exception.
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "",
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.song_dark),
            contentDescription = "",
            modifier = modifier,
        )
    }
}
