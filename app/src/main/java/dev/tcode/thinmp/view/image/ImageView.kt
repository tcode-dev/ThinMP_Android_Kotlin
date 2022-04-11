package dev.tcode.thinmp.view.image

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import dev.tcode.thinmp.R
import java.io.IOException

@Composable
fun ImageView(uri: Uri?, size: Dp? = null) {
    var bitmap: Bitmap? = null
    val source = uri?.let { ImageDecoder.createSource(LocalContext.current.contentResolver, it) }
    val modifier = if (size != null) Modifier.size(size) else Modifier

    try {
        bitmap = source?.let { ImageDecoder.decodeBitmap(it) }
    } catch (e: IOException) {
        // handle exception.
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "",
            modifier = modifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.song_dark),
            contentDescription = "",
            modifier = modifier,
        )
    }
}
