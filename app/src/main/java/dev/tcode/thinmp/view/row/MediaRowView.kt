package dev.tcode.thinmp.view.row

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.R
import java.io.IOException


@Composable
fun MediaRowView(primaryText: String, secondaryText: String, uri: Uri) {
    var bitmap: Bitmap? = null
    val source = ImageDecoder.createSource(LocalContext.current.contentResolver, uri)

    try {
        bitmap = ImageDecoder.decodeBitmap(source)
    } catch (e: IOException) {
        // handle exception.
    }

    Row(modifier = Modifier.padding(10.dp)) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "",
                modifier = Modifier.size(44.dp)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.song_dark),
                contentDescription = "",
                modifier = Modifier.size(44.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(primaryText)
            Text(secondaryText)
        }
    }
}
