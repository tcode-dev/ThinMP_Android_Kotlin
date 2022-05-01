package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import dev.tcode.thinmp.view.divider.DividerView
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.viewModel.AlbumDetailViewModel

@ExperimentalFoundationApi
@Composable
fun AlbumDetailScreen(
    id: String,
    viewModel: AlbumDetailViewModel = AlbumDetailViewModel(
        LocalContext.current,
        id
    )
) {
    val uiState = viewModel.uiState

    Box(Modifier.fillMaxWidth()) {
        Column {
            ConstraintLayout(
                Modifier
                    .fillMaxWidth()
                    .height(LocalConfiguration.current.screenWidthDp.dp)
            ) {
                val (primary, secondary) = createRefs()
                ImageView(
                    uri = uiState.imgUri,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .constrainAs(primary) {
                            top.linkTo(parent.bottom, margin = (-200).dp)
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to MaterialTheme.colors.surface.copy(alpha = 0F),
                                1.0F to MaterialTheme.colors.surface,
                            )
                        ),
                ) {
                    Text(
                        uiState.primaryText,
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        uiState.secondaryText,
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            LazyColumn() {
                items(uiState.songs) { song ->
                    MediaRowView(song.name, song.artistName, song.getUri())
                    DividerView()
                }
            }
        }
    }
}
