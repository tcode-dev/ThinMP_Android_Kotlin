package dev.tcode.thinmp.view.listItem

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.tcode.thinmp.view.image.ImageView

@Composable
fun AlbumListItemView(navController: NavHostController, id: String, primaryText: String, secondaryText: String, uri: Uri) {
    Column(modifier = Modifier.padding(8.dp).clickable {
        navController.navigate("albumDetail/${id}")
    }) {
        ImageView(uri = uri)
        Text(
            primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp)
        )
        Text(
            secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
